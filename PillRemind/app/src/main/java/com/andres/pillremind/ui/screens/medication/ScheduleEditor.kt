package com.andres.pillremind.ui.screens.medication

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.andres.pillremind.R
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.ScheduleRule
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.model.nextOccurrences
import com.andres.pillremind.ui.components.AppDropdownField
import com.andres.pillremind.ui.components.AppFormField
import com.andres.pillremind.ui.components.AppInfoRow
import com.andres.pillremind.ui.components.AppTextField
import com.andres.pillremind.ui.components.WeekDayChipGroup
import com.andres.pillremind.ui.components.labelRes
import com.andres.pillremind.ui.components.timeeditor.TimeListEditor
import com.andres.pillremind.ui.theme.AppSpacing
import java.time.LocalTime

/**
 * Periodicidad seleccionable en el formulario de medicamentos.
 */
enum class SchedulePeriodicity {
    DAILY,
    WEEKLY,
    EVERY_N_DAYS,
    MONTHLY_DAY,
    MONTHLY_WEEKDAY,
    CYCLE,
    SINGLE
}

@StringRes
fun SchedulePeriodicity.labelRes(): Int = when (this) {
    SchedulePeriodicity.DAILY -> R.string.schedule_periodicity_daily
    SchedulePeriodicity.WEEKLY -> R.string.schedule_periodicity_weekly
    SchedulePeriodicity.EVERY_N_DAYS -> R.string.schedule_periodicity_every_n_days
    SchedulePeriodicity.MONTHLY_DAY -> R.string.schedule_periodicity_monthly_day
    SchedulePeriodicity.MONTHLY_WEEKDAY -> R.string.schedule_periodicity_monthly_weekday
    SchedulePeriodicity.CYCLE -> R.string.schedule_periodicity_cycle
    SchedulePeriodicity.SINGLE -> R.string.schedule_periodicity_single
}

/**
 * Estado editable de la pauta de un medicamento.
 *
 * Mantiene los campos de todas las periodicidades para que cambiar de tipo no
 * pierda lo que el usuario ya había escrito.
 */
class ScheduleFormState {

    var periodicity by mutableStateOf(SchedulePeriodicity.WEEKLY)
    var selectedDays by mutableStateOf(emptySet<WeekDay>())
    var intervalDays by mutableStateOf("2")
    var dayOfMonth by mutableStateOf("1")
    var clampToLastDay by mutableStateOf(true)
    var occurrence by mutableIntStateOf(1)
    var monthlyWeekDay by mutableStateOf(WeekDay.MONDAY)
    var onDays by mutableStateOf("5")
    var offDays by mutableStateOf("2")
    val times = mutableStateListOf<LocalTime>()

    /**
     * Convierte el formulario en las reglas que entiende el dominio.
     */
    fun rules(): List<ScheduleRule> = when (periodicity) {
        SchedulePeriodicity.DAILY -> listOf(ScheduleRule.EveryNDays(1))

        SchedulePeriodicity.WEEKLY -> selectedDays
            .sortedBy { it.ordinal }
            .map { ScheduleRule.Weekly(it) }

        SchedulePeriodicity.EVERY_N_DAYS -> listOf(
            ScheduleRule.EveryNDays(intervalDays.toIntOrNull() ?: 0)
        )

        SchedulePeriodicity.MONTHLY_DAY -> listOf(
            ScheduleRule.MonthlyDay(
                dayOfMonth = dayOfMonth.toIntOrNull() ?: 0,
                clampToLastDay = clampToLastDay
            )
        )

        SchedulePeriodicity.MONTHLY_WEEKDAY -> listOf(
            ScheduleRule.MonthlyWeekday(
                occurrence = occurrence,
                day = monthlyWeekDay
            )
        )

        SchedulePeriodicity.CYCLE -> listOf(
            ScheduleRule.Cycle(
                onDays = onDays.toIntOrNull() ?: 0,
                offDays = offDays.toIntOrNull() ?: 0
            )
        )

        SchedulePeriodicity.SINGLE -> listOf(ScheduleRule.Single)
    }

    /**
     * Indica si la pauta y los horarios están completos.
     */
    fun isValid(): Boolean {
        if (times.isEmpty()) return false

        return when (periodicity) {
            SchedulePeriodicity.DAILY,
            SchedulePeriodicity.SINGLE -> true

            SchedulePeriodicity.WEEKLY -> selectedDays.isNotEmpty()

            SchedulePeriodicity.EVERY_N_DAYS ->
                intervalDays.toIntOrNull()?.let { it in 1..365 } == true

            SchedulePeriodicity.MONTHLY_DAY ->
                dayOfMonth.toIntOrNull()?.let { it in 1..31 } == true

            SchedulePeriodicity.MONTHLY_WEEKDAY ->
                occurrence in 1..5 || occurrence == -1

            SchedulePeriodicity.CYCLE -> {
                val on = onDays.toIntOrNull()
                val off = offDays.toIntOrNull()
                on != null && off != null && on >= 1 && off >= 0
            }
        }
    }

    /**
     * Reemplaza la hora de la posición [index].
     *
     * Si la nueva hora ya existe en otra posición, se descarta la entrada
     * editada para no duplicar tomas.
     */
    fun updateTime(index: Int, time: LocalTime) {
        if (index !in times.indices) return

        val existingIndex = times.indexOf(time)
        if (existingIndex != -1 && existingIndex != index) {
            times.removeAt(index)
        } else {
            times[index] = time
        }
    }

    /**
     * Carga la pauta de un medicamento existente.
     *
     * Si hubiera una mezcla de tipos (por ejemplo, tras importar una copia
     * antigua), se usa el primer tipo como base editable.
     */
    fun loadFrom(schedules: List<Schedule>) {
        if (schedules.isEmpty()) return

        times.clear()
        times.addAll(schedules.map { it.time }.distinct().sorted())

        val rules = schedules.map { it.rule }.distinct()

        when {
            rules.all { it is ScheduleRule.Weekly } -> {
                periodicity = SchedulePeriodicity.WEEKLY
                selectedDays = rules
                    .filterIsInstance<ScheduleRule.Weekly>()
                    .map { it.day }
                    .toSet()
            }

            rules.all { it is ScheduleRule.EveryNDays } -> {
                val intervals = rules
                    .filterIsInstance<ScheduleRule.EveryNDays>()
                    .map { it.intervalDays }
                    .distinct()
                val interval = intervals.firstOrNull() ?: 2
                if (interval == 1) {
                    periodicity = SchedulePeriodicity.DAILY
                } else {
                    periodicity = SchedulePeriodicity.EVERY_N_DAYS
                    intervalDays = interval.toString()
                }
            }

            rules.all { it is ScheduleRule.MonthlyDay } -> {
                val rule = rules.first() as ScheduleRule.MonthlyDay
                periodicity = SchedulePeriodicity.MONTHLY_DAY
                dayOfMonth = rule.dayOfMonth.toString()
                clampToLastDay = rule.clampToLastDay
            }

            rules.all { it is ScheduleRule.MonthlyWeekday } -> {
                val rule = rules.first() as ScheduleRule.MonthlyWeekday
                periodicity = SchedulePeriodicity.MONTHLY_WEEKDAY
                occurrence = rule.occurrence
                monthlyWeekDay = rule.day
            }

            rules.all { it is ScheduleRule.Cycle } -> {
                val rule = rules.first() as ScheduleRule.Cycle
                periodicity = SchedulePeriodicity.CYCLE
                onDays = rule.onDays.toString()
                offDays = rule.offDays.toString()
            }

            rules.all { it is ScheduleRule.Single } -> {
                periodicity = SchedulePeriodicity.SINGLE
            }

            else -> applyRuleAsEditable(rules.first())
        }
    }

    private fun applyRuleAsEditable(rule: ScheduleRule) {
        when (rule) {
            is ScheduleRule.Weekly -> {
                periodicity = SchedulePeriodicity.WEEKLY
                selectedDays = setOf(rule.day)
            }

            is ScheduleRule.EveryNDays -> {
                periodicity = if (rule.intervalDays == 1) {
                    SchedulePeriodicity.DAILY
                } else {
                    SchedulePeriodicity.EVERY_N_DAYS
                }
                intervalDays = rule.intervalDays.toString()
            }

            is ScheduleRule.MonthlyDay -> {
                periodicity = SchedulePeriodicity.MONTHLY_DAY
                dayOfMonth = rule.dayOfMonth.toString()
                clampToLastDay = rule.clampToLastDay
            }

            is ScheduleRule.MonthlyWeekday -> {
                periodicity = SchedulePeriodicity.MONTHLY_WEEKDAY
                occurrence = rule.occurrence
                monthlyWeekDay = rule.day
            }

            is ScheduleRule.Cycle -> {
                periodicity = SchedulePeriodicity.CYCLE
                onDays = rule.onDays.toString()
                offDays = rule.offDays.toString()
            }

            ScheduleRule.Single -> periodicity = SchedulePeriodicity.SINGLE
        }
    }
}

/**
 * Saver para conservar el estado editable de la pauta al rotar la pantalla.
 */
val ScheduleFormStateSaver = listSaver<ScheduleFormState, Any>(
    save = { form ->
        listOf(
            form.periodicity.name,
            form.selectedDays.joinToString(",") { it.name },
            form.intervalDays,
            form.dayOfMonth,
            form.clampToLastDay,
            form.occurrence,
            form.monthlyWeekDay.name,
            form.onDays,
            form.offDays,
            form.times.joinToString(",") { it.toString() },
        )
    },
    restore = { values ->
        ScheduleFormState().apply {
            periodicity = SchedulePeriodicity.valueOf(values[0] as String)
            selectedDays = (values[1] as String)
                .split(",")
                .filter { it.isNotBlank() }
                .map { WeekDay.valueOf(it) }
                .toSet()
            intervalDays = values[2] as String
            dayOfMonth = values[3] as String
            clampToLastDay = values[4] as Boolean
            occurrence = values[5] as Int
            monthlyWeekDay = WeekDay.valueOf(values[6] as String)
            onDays = values[7] as String
            offDays = values[8] as String
            times.clear()
            times.addAll(
                (values[9] as String)
                    .split(",")
                    .filter { it.isNotBlank() }
                    .map(LocalTime::parse)
            )
        }
    }
)

private val occurrenceValues = listOf(1, 2, 3, 4, 5, -1)

/**
 * Editor de la pauta de toma: periodicidad, días y horas.
 *
 * @param form Estado editable de la pauta.
 * @param enabled Indica si se puede editar.
 */
@Composable
fun ScheduleEditor(
    form: ScheduleFormState,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        val periodicityLabels = SchedulePeriodicity.entries.map { stringResource(it.labelRes()) }

        AppFormField(label = stringResource(R.string.schedule_periodicity)) {
            AppDropdownField(
                value = stringResource(form.periodicity.labelRes()),
                options = periodicityLabels,
                onOptionSelected = { selected ->
                    val index = periodicityLabels.indexOf(selected)
                    if (index >= 0) {
                        form.periodicity = SchedulePeriodicity.entries[index]
                    }
                },
                placeholder = stringResource(R.string.schedule_select),
                enabled = enabled
            )
        }

        when (form.periodicity) {
            SchedulePeriodicity.DAILY -> Unit

            SchedulePeriodicity.WEEKLY -> AppFormField(
                label = stringResource(R.string.schedule_weekdays)
            ) {
                WeekDayChipGroup(
                    selectedDays = form.selectedDays,
                    onSelectionChange = { form.selectedDays = it },
                    enabled = enabled
                )
            }

            SchedulePeriodicity.EVERY_N_DAYS -> AppFormField(
                label = stringResource(R.string.schedule_interval_days)
            ) {
                AppTextField(
                    value = form.intervalDays,
                    onValueChange = { form.intervalDays = it },
                    placeholder = stringResource(R.string.schedule_interval_example),
                    keyboardType = KeyboardType.Number,
                    enabled = enabled
                )
            }

            SchedulePeriodicity.MONTHLY_DAY -> {
                AppFormField(label = stringResource(R.string.schedule_day_of_month)) {
                    AppTextField(
                        value = form.dayOfMonth,
                        onValueChange = { form.dayOfMonth = it },
                        placeholder = stringResource(R.string.schedule_day_of_month_example),
                        keyboardType = KeyboardType.Number,
                        enabled = enabled
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = form.clampToLastDay,
                        onCheckedChange = { form.clampToLastDay = it },
                        enabled = enabled
                    )
                    Text(
                        text = stringResource(R.string.schedule_clamp_last_day),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            SchedulePeriodicity.MONTHLY_WEEKDAY -> {
                val occurrenceLabels = occurrenceValues.map { occurrenceLabel(it) }
                val weekdayLabels = WeekDay.entries.map { stringResource(it.labelRes()) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    AppFormField(
                        label = stringResource(R.string.schedule_occurrence),
                        modifier = Modifier.weight(1f)
                    ) {
                        AppDropdownField(
                            value = occurrenceLabel(form.occurrence),
                            options = occurrenceLabels,
                            onOptionSelected = { selected ->
                                val index = occurrenceLabels.indexOf(selected)
                                if (index >= 0) {
                                    form.occurrence = occurrenceValues[index]
                                }
                            },
                            placeholder = stringResource(R.string.schedule_select),
                            enabled = enabled
                        )
                    }
                    AppFormField(
                        label = stringResource(R.string.schedule_weekday),
                        modifier = Modifier.weight(1f)
                    ) {
                        AppDropdownField(
                            value = stringResource(form.monthlyWeekDay.labelRes()),
                            options = weekdayLabels,
                            onOptionSelected = { selected ->
                                val index = weekdayLabels.indexOf(selected)
                                if (index >= 0) {
                                    form.monthlyWeekDay = WeekDay.entries[index]
                                }
                            },
                            placeholder = stringResource(R.string.schedule_select),
                            enabled = enabled
                        )
                    }
                }
            }

            SchedulePeriodicity.CYCLE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    AppFormField(
                        label = stringResource(R.string.schedule_on_days),
                        modifier = Modifier.weight(1f)
                    ) {
                        AppTextField(
                            value = form.onDays,
                            onValueChange = { form.onDays = it },
                            placeholder = stringResource(R.string.schedule_cycle_example),
                            keyboardType = KeyboardType.Number,
                            enabled = enabled
                        )
                    }
                    AppFormField(
                        label = stringResource(R.string.schedule_off_days),
                        modifier = Modifier.weight(1f)
                    ) {
                        AppTextField(
                            value = form.offDays,
                            onValueChange = { form.offDays = it },
                            placeholder = stringResource(R.string.schedule_cycle_example),
                            keyboardType = KeyboardType.Number,
                            enabled = enabled
                        )
                    }
                }
            }

            SchedulePeriodicity.SINGLE -> Text(
                text = stringResource(R.string.schedule_single_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = AppSpacing.xs)
            )
        }

        AppFormField(label = stringResource(R.string.horario)) {
            TimeListEditor(
                times = form.times,
                enabled = enabled,
                onAddClick = { time -> form.times.add(time) },
                onRemoveTime = { time -> form.times.remove(time) },
                onUpdateTime = { index, time -> form.updateTime(index, time) }
            )
        }
    }
}

/**
 * Resumen de solo lectura de la pauta, sin campos deshabilitados.
 *
 * @param form Estado editable de la pauta que se quiere mostrar.
 */
@Composable
fun ScheduleReadOnly(form: ScheduleFormState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AppInfoRow(
            label = stringResource(R.string.schedule_periodicity),
            value = stringResource(form.periodicity.labelRes()),
        )

        when (form.periodicity) {
            SchedulePeriodicity.WEEKLY -> {
                val selectedDayLabels = mutableListOf<String>()
                for (day in WeekDay.entries) {
                    if (day in form.selectedDays) {
                        selectedDayLabels.add(stringResource(day.labelRes()))
                    }
                }
                AppInfoRow(
                    label = stringResource(R.string.schedule_selected_days),
                    value = selectedDayLabels.joinToString(", "),
                )
            }

            SchedulePeriodicity.EVERY_N_DAYS -> AppInfoRow(
                label = stringResource(R.string.schedule_interval_days),
                value = stringResource(
                    R.string.schedule_summary_every_n_days,
                    form.intervalDays.toIntOrNull() ?: 0,
                ),
            )

            SchedulePeriodicity.MONTHLY_DAY -> AppInfoRow(
                label = stringResource(R.string.schedule_day_of_month),
                value = stringResource(
                    R.string.schedule_summary_day_of_month,
                    form.dayOfMonth.toIntOrNull() ?: 0,
                ),
            )

            SchedulePeriodicity.MONTHLY_WEEKDAY -> {
                val occurrence = occurrenceLabel(form.occurrence)
                val day = stringResource(form.monthlyWeekDay.labelRes())
                AppInfoRow(
                    label = stringResource(R.string.schedule_occurrence),
                    value = stringResource(
                        R.string.schedule_summary_monthly_weekday,
                        occurrence,
                        day,
                    ),
                )
            }

            SchedulePeriodicity.CYCLE -> AppInfoRow(
                label = stringResource(R.string.schedule_cycle_detail),
                value = stringResource(
                    R.string.schedule_summary_cycle,
                    form.onDays.toIntOrNull() ?: 0,
                    form.offDays.toIntOrNull() ?: 0,
                ),
            )

            SchedulePeriodicity.DAILY,
            SchedulePeriodicity.SINGLE -> Unit
        }

        AppInfoRow(
            label = stringResource(R.string.horario),
            value = form.times.joinToString(", ") { it.toString() },
        )
    }
}

@Composable
private fun occurrenceLabel(value: Int): String = stringResource(
    when (value) {
        1 -> R.string.schedule_occurrence_first
        2 -> R.string.schedule_occurrence_second
        3 -> R.string.schedule_occurrence_third
        4 -> R.string.schedule_occurrence_fourth
        5 -> R.string.schedule_occurrence_fifth
        else -> R.string.schedule_occurrence_last
    }
)
