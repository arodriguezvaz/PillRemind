package com.andres.pillremind.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centraliza la cancelación de notificaciones de recordatorio.
 *
 * Las notificaciones usan el ID de la toma, así que al marcar, editar, archivar
 * o importar datos hay que retirarlas para que no quede un aviso visible de una
 * toma que ya no existe o que pertenece a otro conjunto de datos.
 */
@Singleton
class ReminderNotifications @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Cancela la notificación asociada a una toma.
     *
     * @param intakeId Identificador de la toma.
     */
    fun cancel(intakeId: Int) {
        NotificationManagerCompat.from(context).cancel(intakeId)
    }

    /**
     * Cancela las notificaciones asociadas a una colección de tomas.
     *
     * @param intakeIds Identificadores de las tomas.
     */
    fun cancelAll(intakeIds: Collection<Int>) {
        intakeIds.forEach(::cancel)
    }

    /**
     * Cancela todas las notificaciones de la aplicación.
     *
     * Se usa al borrar un perfil o importar una copia, donde los IDs pueden
     * reutilizarse para tomas distintas.
     */
    fun cancelAll() {
        NotificationManagerCompat.from(context).cancelAll()
    }
}
