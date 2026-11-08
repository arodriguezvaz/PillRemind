package com.andres.pillremind.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.content.pm.PackageInfoCompat
import com.andres.pillremind.data.local.update.UpdatePreferences
import com.andres.pillremind.data.remote.GitHubUpdateDataSource
import com.andres.pillremind.domain.model.InstalledAppInfo
import com.andres.pillremind.domain.model.UpdateInfo
import com.andres.pillremind.domain.repository.UpdateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del repositorio de actualizaciones.
 *
 * La consulta remota nunca borra la caché anterior si falla, de modo que un
 * bloqueo ya conocido se mantiene aunque el dispositivo se quede sin red.
 *
 * @property dataSource Descarga y validación del manifiesto.
 * @property preferences Caché local de la última información.
 * @property context Contexto para leer la versión instalada.
 */
@Singleton
class UpdateRepositoryImpl @Inject constructor(
    private val dataSource: GitHubUpdateDataSource,
    private val preferences: UpdatePreferences,
    @ApplicationContext private val context: Context,
) : UpdateRepository {

    override val cachedInfo: Flow<UpdateInfo?> = preferences.cachedInfo
    override val dismissedVersionCode: Flow<Long?> = preferences.dismissedVersionCode
    override val lastCheckAt: Flow<Long?> = preferences.lastCheckAt

    override suspend fun refresh(): Result<UpdateInfo> {
        val checkedAt = System.currentTimeMillis()
        return try {
            val info = dataSource.fetchLatest()
            preferences.saveCheck(info, checkedAt)
            Result.success(info)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // La marca se guarda también si falla la consulta para que la
            // comprobación automática no se repita en cada arranque.
            try {
                preferences.saveLastCheckAt(checkedAt)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                // La caché anterior sigue siendo válida.
            }
            Result.failure(e)
        }
    }

    override suspend fun dismiss(versionCode: Long) {
        preferences.dismiss(versionCode)
    }

    override suspend fun clearDismissal() {
        preferences.clearDismissal()
    }

    override fun installedAppInfo(): InstalledAppInfo {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return InstalledAppInfo(
            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
            versionName = packageInfo.versionName.orEmpty(),
            isDebuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0,
        )
    }
}
