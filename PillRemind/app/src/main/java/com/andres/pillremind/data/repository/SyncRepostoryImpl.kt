package com.andres.pillremind.data.repository


import android.util.Log
import com.andres.pillremind.data.remote.dto.IntakeDto
import com.andres.pillremind.data.remote.dto.MedicationDto
import com.andres.pillremind.data.remote.dto.ProfileDto
import com.andres.pillremind.data.remote.dto.ScheduleDto
import com.andres.pillremind.data.remote.dto.UsuarioDto
import com.andres.pillremind.domain.model.Intake
import com.andres.pillremind.domain.model.Medication
import com.andres.pillremind.domain.model.Profile
import com.andres.pillremind.domain.model.Schedule
import com.andres.pillremind.domain.model.WeekDay
import com.andres.pillremind.domain.repository.IntakeRepository
import com.andres.pillremind.domain.repository.MedicationRepository
import com.andres.pillremind.domain.repository.ProfileRepository
import com.andres.pillremind.domain.repository.ScheduleRepository
import com.andres.pillremind.domain.repository.SyncRepository
import com.andres.pillremind.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import jakarta.inject.Inject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class SyncRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val intakeRepository: IntakeRepository
) : SyncRepository {

    /**
     * Sincroniza todas las entidades de la aplicación con Supabase.
     *
     * La sincronización se realiza en orden para respetar las dependencias
     * entre entidades (usuario → perfiles → medicamentos → horarios → tomas).
     */
    override suspend fun syncCompleto(): Result<Unit> = runCatching {
        syncUsuario().getOrThrow()
        syncPerfiles().getOrThrow()
        syncMedicamentos().getOrThrow()
        syncSchedules().getOrThrow()
        syncIntakes().getOrThrow()
    }

    /**
     * Elimina los datos locales y los vuelve a descargar desde Supabase.
     */
    override suspend fun borrarLocalYDescargar(): Result<Unit> = runCatching {
        val user = userRepository.getUser() ?: error("No hay usuario local")

        // Borrar datos locales en orden el resto se borra con cascade
        medicationRepository.deleteAllByUsuario(user.id)
        profileRepository.deleteAllByUsuario(user.id)

        //  Descargar y reconstruir los datos desde Supabase
        downloadCompleto().getOrThrow()
    }

    /**
     * Descarga todos los datos del usuario desde Supabase y reconstruye
     * la información en Room.
     *
     * Orden de descarga:
     * perfiles → medicamentos → horarios → tomas.
     */
    override suspend fun downloadCompleto(): Result<Unit> = runCatching {
        //Verifica que haya sesión activa
        auth.currentUserOrNull() ?: error("No hay sesión")
        //Obtiene el usuario invitado creado en Room
        val user = userRepository.getUser() ?: error("No hay usuario local")

        // Descargar y guardar perfiles
        val perfilesRemoto = postgrest.from("profiles")
            .select()
            .decodeList<ProfileDto>()

        perfilesRemoto.forEach { dto ->
            val profileId = profileRepository.insertProfile(
                Profile(
                    usuarioId = user.id,
                    supabaseId = dto.id,
                    name = dto.name,
                    urlImagen = dto.imageUrl
                )
            ).toInt()

            //Descargar medicamentos de cada perfil
            val medicamentosRemoto = postgrest.from("medications")
                .select { filter { eq("profile_id", dto.id!!) } }
                .decodeList<MedicationDto>()
            //Inserta los medicamentos del remoto en Room
            medicamentosRemoto.forEach { medDto ->
                val medicationId = medicationRepository.insertMedication(
                    Medication(
                        profileId = profileId,
                        supabaseId = medDto.id,
                        name = medDto.name,
                        dose = medDto.dose,
                        unit = medDto.unit,
                        isChronic = medDto.isChronic,
                        startDate = LocalDate.parse(medDto.startDate),
                        endDate = medDto.endDate?.let { LocalDate.parse(it) },
                        imageUrl = medDto.imageUrl,
                        leafletUrl = medDto.leafletUrl
                    )
                ).toInt()

                // Descargar schedules de cada medicamento
                val schedulesRemoto = postgrest.from("schedules")
                    .select { filter { eq("medication_id", medDto.id!!) } }
                    .decodeList<ScheduleDto>()
                //Inserta los horarios del remoto en Room
                schedulesRemoto.forEach { schedDto ->
                    val scheduleId = scheduleRepository.insertSchedule(
                        Schedule(
                            medicationId = medicationId,
                            supabaseId = schedDto.id,
                            weekDay = WeekDay.valueOf(schedDto.weekDay),
                            time = LocalTime.parse(schedDto.time)
                        )
                    ).toInt()

                    // Descargar schedules de cada medicamento
                    val intakesRemoto = postgrest.from("intakes")
                        .select { filter { eq("schedule_id", schedDto.id!!) } }
                        .decodeList<IntakeDto>()
                    //Inserta las tomas del remoto en Room
                    intakesRemoto.forEach { intakeDto ->
                        intakeRepository.insertIntake(
                            Intake(
                                scheduleId = scheduleId,
                                supabaseId = intakeDto.id,
                                scheduledAt = LocalDateTime.parse(intakeDto.scheduledAt),
                                takenAt = intakeDto.takenAt?.let { LocalDateTime.parse(it) }
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Sincroniza el usuario local con la tabla `usuarios` de Supabase.
     *
     * Si el usuario aún no tiene asociado un identificador de Supabase,
     * se actualiza el registro local tras la sincronización.
     */
    override suspend fun syncUsuario(): Result<Unit> = runCatching {
        val supabaseUid = auth.currentUserOrNull()?.id ?: error("No hay sesión")
        Log.d("Sync", "Sincronizando usuario con uid=$supabaseUid")

        val user = userRepository.getUser() ?: error("No hay usuario local")
        Log.d("Sync", "esPremium a subir: ${user.esPremium}")
        Log.d("Sync", "tipoUsuario a subir: ${user.tipoUsuario.name}")
        val dto = UsuarioDto(
            userId = supabaseUid,
            tipoUsuario = user.tipoUsuario.name,
            email = user.email,
            esPremium = user.esPremium,
            aceptaComunicaciones = user.aceptaComunicaciones
        )

        // Inserta el usuario en Supabase
        // Si ya existe un registro con el mismo user_id(supabaseId en room), se actualiza en lugar de crear uno nuevo.
        // Se devuelve el registro resultante para obtener los identificadores generados.
        val resultado = postgrest.from("usuarios")
            .upsert(dto) {
                // Si ya existe un usario con este id, se actualiza.
                onConflict = "user_id"
                //Devuelve el usario actualizado
                select()
            }
            .decodeSingle<UsuarioDto>()

        // Asociar el identificador remoto al usuario local la primera vez que se sincroniza
        if (user.supabaseUid == null) {
            userRepository.updateUser(
                user.copy(
                    supabaseUid = resultado.id,
                    syncPendiente = false,
                    updatedAt = LocalDateTime.now()
                )
            )
        }
    }

    //Comentar esto y revisar por si acaso
    override suspend fun syncPerfiles(): Result<Unit> = runCatching {
        val supabaseUid = auth.currentUserOrNull()?.id ?: error("No hay sesión")
        Log.d("SyncPerfiles", "supabaseUid=$supabaseUid")

        val user = userRepository.getUser() ?: error("No hay usuario local")
        Log.d("SyncPerfiles", "usuario local id=${user.id} esPremium=${user.esPremium}")

        val perfilesLocales = profileRepository.getAllByUser(user.id)
        Log.d("SyncPerfiles", "perfiles a sincronizar: ${perfilesLocales.size}")

        perfilesLocales.forEach { perfil ->
            Log.d(
                "SyncPerfiles",
                "sincronizando perfil id=${perfil.id} supabaseId=${perfil.supabaseId} name=${perfil.name}"
            )

            try {
                val dto = ProfileDto(
                    id = perfil.supabaseId,
                    userId = supabaseUid,
                    name = perfil.name,
                    imageUrl = perfil.urlImagen
                )
                Log.d("SyncPerfiles", "dto a enviar: $dto")

                val resultado = postgrest.from("profiles")
                    .upsert(dto) { onConflict = "id"; select() }
                    .decodeSingle<ProfileDto>()

                Log.d("SyncPerfiles", "respuesta Supabase: $resultado")

                if (perfil.supabaseId == null) {
                    profileRepository.updateProfile(perfil.copy(supabaseId = resultado.id))
                    Log.d("SyncPerfiles", "supabaseId guardado en Room: ${resultado.id}")
                }
            } catch (e: Exception) {
                Log.e("SyncPerfiles", "error sincronizando perfil ${perfil.id}: ${e.message}", e)
            }
        }
    }

    override suspend fun syncMedicamentos(): Result<Unit> = runCatching {
        auth.currentUserOrNull()?.id ?: error("No hay sesión")
        val user = userRepository.getUser() ?: error("No hay usuario local")
        val medicamentos = medicationRepository.getAllByUser(user.id)

        medicamentos.forEach { med ->
            val profileSupabaseId = profileRepository.getProfileByIdOnce(med.profileId)
                ?.supabaseId ?: return@forEach

            val dto = MedicationDto(
                id = med.supabaseId,
                profileId = profileSupabaseId,
                name = med.name,
                dose = med.dose,
                unit = med.unit,
                isChronic = med.isChronic,
                startDate = med.startDate.toString(),
                endDate = med.endDate?.toString(),
                imageUrl = med.imageUrl,
                leafletUrl = med.leafletUrl
            )

            val resultado = postgrest.from("medications")
                .upsert(dto) {
                    select()
                }
                .decodeSingle<MedicationDto>()

            if (med.supabaseId == null) {
                medicationRepository.updateMedication(
                    med.copy(supabaseId = resultado.id)
                )
            }
        }
    }

    override suspend fun syncSchedules(): Result<Unit> = runCatching {
        val user = userRepository.getUser() ?: error("No hay usuario local")
        val schedules = scheduleRepository.getAllByUser(user.id)

        schedules.forEach { schedule ->
            val medicationSupabaseId =
                medicationRepository.getMedicationByIdOnce(schedule.medicationId)
                    ?.supabaseId ?: return@forEach

            val dto = ScheduleDto(
                id = schedule.supabaseId,
                medicationId = medicationSupabaseId,
                weekDay = schedule.weekDay.name,
                time = schedule.time.toString()
            )

            val resultado = postgrest.from("schedules")
                .upsert(dto) { select() }
                .decodeSingle<ScheduleDto>()

            if (schedule.supabaseId == null) {
                scheduleRepository.updateSchedule(
                    schedule.copy(supabaseId = resultado.id)
                )
            }
        }
    }

    override suspend fun syncIntakes(): Result<Unit> = runCatching {
        val user = userRepository.getUser() ?: error("No hay usuario local")
        val intakes = intakeRepository.getAllByUser(user.id)

        intakes.forEach { intake ->
            val scheduleSupabaseId = scheduleRepository.getScheduleByIdOnce(intake.scheduleId)
                ?.supabaseId ?: return@forEach

            val dto = IntakeDto(
                id = intake.supabaseId,
                scheduleId = scheduleSupabaseId,
                scheduledAt = intake.scheduledAt.toString(),
                takenAt = intake.takenAt?.toString()
            )

            val resultado = postgrest.from("intakes")
                .upsert(dto) { select() }
                .decodeSingle<IntakeDto>()

            if (intake.supabaseId == null) {
                intakeRepository.updateIntake(
                    intake.copy(supabaseId = resultado.id)
                )
            }
        }
    }

    override suspend fun getUsuarioRemoto(supabaseUid: String): UsuarioDto? {
        return try {
            postgrest.from("usuarios")
                .select { filter { eq("user_id", supabaseUid) } }
                .decodeSingleOrNull<UsuarioDto>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteProfile(supabaseId: String): Result<Unit> = runCatching {
        postgrest.from("profiles")
            .delete {
                filter { eq("id", supabaseId) }
            }
    }

    override suspend fun deleteMedication(supabaseId: String): Result<Unit> = runCatching {
        postgrest.from("medications")
            .delete {
                filter { eq("id", supabaseId) }
            }
    }
}