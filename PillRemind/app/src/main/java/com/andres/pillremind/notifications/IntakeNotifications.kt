package com.andres.pillremind.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Gestiona las notificaciones de recordatorio asociadas a una toma.
 *
 * El identificador de la notificación es el `intakeId`, igual que en
 * [AlarmNotificationHandler]. Centralizar la cancelación evita que cada pantalla
 * que marca una toma tenga que conocer ese detalle.
 *
 * @property context Contexto utilizado para cancelar la notificación.
 */
class IntakeNotifications @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Cancela la notificación de recordatorio de una toma.
     *
     * Es seguro llamarlo aunque la notificación ya no esté visible.
     *
     * @param intakeId Identificador de la toma cuya notificación se cancela.
     */
    fun cancel(intakeId: Int) {
        if (intakeId <= 0) return
        NotificationManagerCompat.from(context).cancel(intakeId)
    }

    /**
     * Cancela las notificaciones de varias tomas.
     *
     * El trabajo se hace fuera del hilo principal porque un medicamento crónico
     * puede acumular muchos identificadores históricos.
     *
     * @param intakeIds Identificadores de las tomas cuyas notificaciones se cancelan.
     */
    suspend fun cancelAll(intakeIds: Collection<Int>) = withContext(Dispatchers.IO) {
        intakeIds.forEach { intakeId -> cancel(intakeId) }
    }

    /**
     * Cancela todas las notificaciones de recordatorio de la app.
     *
     * Se usa al importar una copia de seguridad, porque los datos anteriores
     * desaparecen y sus notificaciones dejarían de ser válidas.
     */
    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
