package com.andres.pillremind.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.andres.pillremind.data.backup.BackupArchive
import com.andres.pillremind.data.backup.BackupCrypto
import com.andres.pillremind.data.backup.BackupError
import com.andres.pillremind.data.backup.BackupException
import com.andres.pillremind.data.backup.BackupImageStore
import com.andres.pillremind.data.backup.BackupSerializer
import com.andres.pillremind.data.backup.BackupSnapshot
import com.andres.pillremind.data.backup.BackupValidator
import com.andres.pillremind.data.backup.MAX_BACKUP_PAYLOAD_BYTES
import com.andres.pillremind.data.backup.readBytesLimited
import com.andres.pillremind.data.backup.toBackupFile
import com.andres.pillremind.data.backup.toEntities
import com.andres.pillremind.data.local.dao.BackupDao
import com.andres.pillremind.data.local.database.AppDatabase
import com.andres.pillremind.data.local.session.SessionManager
import com.andres.pillremind.domain.repository.BackupRepository
import com.andres.pillremind.notifications.ReminderNotifications
import com.andres.pillremind.notifications.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
/**
 * Implementación de la copia de seguridad local.
 *
 * Exporta todas las tablas y las imágenes referenciadas en un contenedor ZIP.
 * La importación valida el archivo completo antes de reemplazar la base de
 * datos, cancela las alarmas antiguas y regenera la ventana de recordatorios.
 */
class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val backupDao: BackupDao,
    private val imageStore: BackupImageStore,
    private val sessionManager: SessionManager,
    private val reminderScheduler: ReminderScheduler,
    private val notifications: ReminderNotifications,
    @ApplicationContext private val context: Context
) : BackupRepository {

    override suspend fun isEncrypted(input: InputStream): Boolean =
        withContext(Dispatchers.IO) {
            BackupCrypto.readHeader(input).encrypted
        }

    override suspend fun exportTo(output: OutputStream, password: CharArray?) =
        withContext(Dispatchers.IO) {
            val snapshot = database.withTransaction {
                BackupSnapshot(
                    users = backupDao.getUsersOnce(),
                    profiles = backupDao.getProfilesOnce(),
                    medications = backupDao.getMedicationsOnce(),
                    schedules = backupDao.getSchedulesOnce(),
                    intakes = backupDao.getIntakesOnce()
                )
            }

            val imageFiles = imageStore.collectImages(snapshot.profiles, snapshot.medications)
            val archivePaths = imageStore.buildArchivePaths(imageFiles)
            val file = snapshot.toBackupFile(
                imagePaths = archivePaths,
                appVersion = appVersion()
            )
            val archiveImages = archivePaths.entries.associate { (absolutePath, archivePath) ->
                archivePath to imageFiles.getValue(absolutePath)
            }

            BackupCrypto.writeHeader(output, encrypted = password != null)
            val payload = if (password != null) {
                BackupCrypto.encryptingStream(output, password)
            } else {
                output
            }
            BackupArchive.write(payload, BackupSerializer.encode(file), archiveImages)
        }

    override suspend fun importFrom(input: InputStream, password: CharArray?) =
        withContext(Dispatchers.IO) {
            val header = BackupCrypto.readHeader(input)
            val payload = if (header.encrypted) {
                val secret = password
                    ?: throw BackupException(
                        BackupError.PASSWORD_REQUIRED,
                        "Esta copia está protegida con contraseña"
                    )
                BackupCrypto.decryptingStream(input, secret)
            } else {
                input
            }

            val contents = try {
                // Se lee el payload completo para que GCM valide la etiqueta de
                // integridad antes de interpretar el ZIP. La lectura está
                // acotada para no agotar la memoria con archivos enormes.
                val payloadBytes = payload.use { it.readBytesLimited(MAX_BACKUP_PAYLOAD_BYTES) }
                BackupArchive.read(ByteArrayInputStream(payloadBytes))
            } catch (e: Exception) {
                if (e is BackupException) throw e
                if (header.encrypted) {
                    throw BackupException(
                        BackupError.WRONG_PASSWORD,
                        "Contraseña incorrecta o archivo dañado",
                        e
                    )
                }
                throw BackupException(
                    BackupError.CORRUPTED,
                    "El archivo de copia está dañado",
                    e
                )
            }

            val backup = BackupSerializer.decode(contents.json)
            BackupValidator.validate(backup)

            val referencedImages = (
                backup.profiles.mapNotNull { it.imagePath } +
                    backup.medications.mapNotNull { it.imagePath }
                ).toSet()
            val missingImages = referencedImages - contents.images.keys
            if (missingImages.isNotEmpty()) {
                throw BackupException(
                    BackupError.MISSING_IMAGES,
                    "La copia no contiene todas las imágenes referenciadas"
                )
            }

            val imagesToRestore = contents.images.filterKeys { it in referencedImages }

            val oldImages = database.withTransaction {
                (
                    backupDao.getProfilesOnce().mapNotNull { it.imageUrl } +
                        backupDao.getMedicationsOnce().mapNotNull { it.imageUrl }
                    ).toSet()
            }
            val oldFutureIntakeIds = backupDao.getFutureIntakeIds(LocalDateTime.now())

            val newImages = imageStore.restoreImages(imagesToRestore)
            val entities = try {
                backup.toEntities(newImages)
            } catch (e: Exception) {
                imageStore.deleteImages(newImages.values)
                throw e
            }

            try {
                database.withTransaction {
                    backupDao.deleteIntakes()
                    backupDao.deleteSchedules()
                    backupDao.deleteMedications()
                    backupDao.deleteProfiles()
                    backupDao.deleteUsers()

                    backupDao.insertUsers(entities.users)
                    backupDao.insertProfiles(entities.profiles)
                    backupDao.insertMedications(entities.medications)
                    backupDao.insertSchedules(entities.schedules)
                    backupDao.insertIntakes(entities.intakes)
                }
            } catch (e: Exception) {
                imageStore.deleteImages(newImages.values)
                throw BackupException(
                    BackupError.SAVE_ERROR,
                    "No se pudieron guardar los datos importados",
                    e
                )
            }

            reminderScheduler.cancelAlarms(oldFutureIntakeIds)
            notifications.cancelAll()
            sessionManager.clearActiveProfile()
            sessionManager.setActiveUser(entities.users.first().id)
            sessionManager.clearLastGeneratedDate()

            try {
                reminderScheduler.generateWindow()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    "BackupRepository",
                    "No se pudieron reprogramar los recordatorios tras la importación",
                    e
                )
            }

            imageStore.deleteImages(oldImages - newImages.values.toSet())
        }

    private fun appVersion(): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
}
