package com.andres.pillremind.notifications

import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.repository.IntakeRepository
import javax.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Coordina la generación de tomas y la programación de sus alarmas.
 *
 * Centraliza la lógica que antes repetían HomeViewModel, MedicationViewModel y
 * DailyIntakeWorker, para que todos programen alarmas sobre los IDs reales de Room.
 *
 * @param intakeRepository Repositorio de tomas.
 * @param alarmScheduler Programador de alarmas del sistema.
 */
class ReminderScheduler @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val alarmScheduler: AlarmScheduler,
) {

    companion object {
        /**
         * Número de días hacia delante que se mantienen generados y programados.
         *
         * Una ventana semanal cubre un ciclo completo de horarios y da margen
         * suficiente para absorber retrasos del worker diario sin dejar huecos.
         */
        const val WINDOW_DAYS = 7L
    }

    /**
     * Genera la ventana rodante de [WINDOW_DAYS] días y programa sus alarmas.
     *
     * El rango empieza en la última fecha generada (o en hoy, si esa fecha es
     * futura) y termina en `hoy + WINDOW_DAYS - 1`. La inserción es idempotente:
     * los días que ya existían no se duplican y conservan su ID real.
     *
     * @param today Día de referencia para calcular la ventana.
     * @param now Momento de referencia para decidir qué tomas son futuras.
     * @return Tomas generadas, incluidas las que ya existían, con su ID real.
     */
    suspend fun generateWindow(
        today: LocalDate = LocalDate.now(),
        now: LocalDateTime = LocalDateTime.now()
    ): List<Intake> {
        val desde = intakeRepository.getLastGeneratedDate()
        val hasta = windowEnd(today)
        val inicio = if (desde.isBefore(today)) desde else today
        val intakes = generateAndSchedule(inicio, hasta, now)
        intakeRepository.saveLastGeneratedDate(hasta)
        return intakes
    }

    /**
     * Calcula el último día de la ventana rodante.
     *
     * @param today Día de referencia.
     */
    fun windowEnd(today: LocalDate = LocalDate.now()): LocalDate =
        today.plusDays(WINDOW_DAYS - 1)

    /**
     * Genera las tomas del rango indicado y programa las alarmas de las futuras.
     *
     * @param desde Primer día a generar.
     * @param hasta Último día a generar.
     * @param now Momento de referencia para decidir qué tomas son futuras.
     * @return Tomas generadas, incluidas las que ya existían, con su ID real.
     */
    suspend fun generateAndSchedule(
        desde: LocalDate,
        hasta: LocalDate,
        now: LocalDateTime = LocalDateTime.now()
    ): List<Intake> {
        // Si la última generación quedó en una fecha posterior (por ejemplo,
        // porque el worker ya generó mañana), se genera al menos el día `hasta`
        // para no dejar sin tomas el día actual.
        val inicio = if (desde.isAfter(hasta)) hasta else desde
        val intakes = intakeRepository.createIntakes(inicio, hasta)
        scheduleFuture(intakes, now)
        return intakes
    }

    /**
     * Programa alarmas solo para tomas con ID válido y hora futura.
     *
     * @param intakes Tomas a programar.
     * @param now Momento de referencia para descartar las ya pasadas.
     */
    fun scheduleFuture(
        intakes: List<Intake>,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        val nowMillis = now.toEpochMillis()

        intakes.forEach { intake ->
            if (intake.id <= 0) return@forEach

            val triggerAtMillis = intake.scheduledAt.toEpochMillis()
            if (triggerAtMillis > nowMillis) {
                alarmScheduler.schedule(intake.id, triggerAtMillis)
            }
        }
    }

    /**
     * Cancela las alarmas asociadas a una lista de tomas.
     *
     * Se usa cuando ya se han guardado los IDs de las tomas futuras que van a
     * desaparecer y la transacción de base de datos ha terminado bien.
     *
     * @param intakeIds IDs de las tomas cuyas alarmas se cancelan.
     */
    fun cancelAlarms(intakeIds: List<Int>) {
        intakeIds.forEach { intakeId -> alarmScheduler.cancel(intakeId) }
    }

    /**
     * Cancela las alarmas de las tomas futuras de un medicamento.
     *
     * @param medicationId Medicamento cuyas alarmas se cancelan.
     * @param from Momento desde el que se consideran tomas futuras.
     */
    suspend fun cancelFutureForMedication(
        medicationId: Int,
        from: LocalDateTime = LocalDateTime.now()
    ) {
        intakeRepository.getFutureIntakesByMedicationId(medicationId, from)
            .forEach { intake -> alarmScheduler.cancel(intake.id) }
    }

    /**
     * Cancela las alarmas futuras de todos los medicamentos de un perfil.
     *
     * Se usa antes de borrar el perfil para no dejar alarmas huérfanas en
     * `AlarmManager` apuntando a tomas que Room eliminará en cascada.
     *
     * @param profileId Perfil cuyas alarmas se cancelan.
     * @param from Momento desde el que se consideran tomas futuras.
     */
    suspend fun cancelFutureForProfile(
        profileId: Int,
        from: LocalDateTime = LocalDateTime.now()
    ) {
        intakeRepository.getFutureIntakesByProfileId(profileId, from)
            .forEach { intake -> alarmScheduler.cancel(intake.id) }
    }

    private fun LocalDateTime.toEpochMillis(): Long =
        atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}
