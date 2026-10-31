package com.andres.pillremind.data.backup

import android.content.Context
import com.andres.pillremind.data.local.entity.MedicationEntity
import com.andres.pillremind.data.local.entity.ProfileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestiona las imágenes incluidas en la copia de seguridad.
 *
 * Las imágenes viven en el almacenamiento interno de la aplicación. En el ZIP
 * se guardan con rutas relativas y al importar se escriben con nombres nuevos
 * para no depender de las rutas del dispositivo original.
 */
@Singleton
class BackupImageStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")

    /**
     * Recoge las imágenes existentes referenciadas por perfiles y medicamentos.
     *
     * @return Mapa de ruta absoluta a archivo, solo con archivos válidos dentro
     * del almacenamiento interno de la aplicación.
     */
    fun collectImages(
        profiles: List<ProfileEntity>,
        medications: List<MedicationEntity>
    ): Map<String, File> {
        val paths = profiles.mapNotNull { it.imageUrl } +
            medications.mapNotNull { it.imageUrl }

        return paths.distinct().mapNotNull { path ->
            val file = File(path)
            if (file.isFile && isInsideFilesDir(file)) {
                path to file
            } else {
                null
            }
        }.toMap()
    }

    /**
     * Asigna una ruta relativa estable a cada imagen.
     *
     * @return Mapa de ruta absoluta a ruta dentro del ZIP.
     */
    fun buildArchivePaths(images: Map<String, File>): Map<String, String> =
        images.keys.sorted().mapIndexed { index, absolutePath ->
            val extension = images.getValue(absolutePath).extension
                .lowercase()
                .takeIf { it in allowedExtensions }
                ?: "jpg"
            val number = (index + 1).toString().padStart(4, '0')
            absolutePath to "images/img_$number.$extension"
        }.toMap()

    /**
     * Escribe las imágenes del ZIP en el almacenamiento interno.
     *
     * @param images Mapa de ruta relativa del ZIP a contenido.
     * @return Mapa de ruta relativa del ZIP a ruta absoluta restaurada.
     */
    fun restoreImages(images: Map<String, ByteArray>): Map<String, String> {
        val created = mutableListOf<File>()
        return try {
            images.mapValues { (archivePath, bytes) ->
                val extension = archivePath.substringAfterLast('.', "jpg")
                    .lowercase()
                    .takeIf { it in allowedExtensions }
                    ?: "jpg"
                val file = File(context.filesDir, "backup_${UUID.randomUUID()}.$extension")
                file.outputStream().use { output -> output.write(bytes) }
                created += file
                file.absolutePath
            }
        } catch (e: Exception) {
            created.forEach { file -> runCatching { file.delete() } }
            throw BackupException("No se pudieron restaurar las imágenes", e)
        }
    }

    /**
     * Borra imágenes antiguas que ya no se usan.
     *
     * Solo elimina archivos dentro del almacenamiento interno de la aplicación.
     */
    fun deleteImages(paths: Collection<String>) {
        paths.forEach { path ->
            val file = File(path)
            if (isInsideFilesDir(file)) {
                runCatching { file.delete() }
            }
        }
    }

    private fun isInsideFilesDir(file: File): Boolean =
        runCatching {
            val root = context.filesDir.canonicalFile
            val target = file.canonicalFile
            target.path.startsWith(root.path + File.separator)
        }.getOrDefault(false)
}
