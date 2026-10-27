# AGENTS.md

## Project layout

- Android app only. The Gradle root is `PillRemind/` (single `:app` module); the repo root also contains the academic write-up in `doc/`. Run Gradle commands from `PillRemind/`, not from the repo root.
- Package is `com.andres.pillremind`. Entrypoints: `MainActivity` (Compose UI) and `PillRemindApp` (Hilt/Application setup).
- Docs are Spanish/Galician, and code comments, KDoc, and UI strings are Spanish. Keep identifiers in English and match the surrounding style.

## Branch context

This repository keeps the current local-only app on `master` and archived branches
of the earlier Supabase version. Check the current branch with
`git branch --show-current` before assuming the architecture:

- `master`: current local-only app. No Supabase, authentication, sync, or INTERNET permission.
- `local/sin-nube`: same local-only version, kept as the working branch that was merged into `master`.
- `respaldo/nube`: archived cloud version from the old `master` (no notification fixes).
- `respaldo/integridad` and `fix/integridad`: archived cloud version with notification and sync fixes.
- `fix/notificaciones`: archived cloud version with notification fixes only.

Do not reintroduce Supabase into `master` or `local/sin-nube`. Do not apply the
local-only cleanup on the archive branches: Supabase is still part of their code.

## Commands

```bash
cd PillRemind
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "com.andres.pillremind.notifications.ReminderSchedulerTest"
./gradlew connectedDebugAndroidTest   # device/emulator required
./gradlew lintDebug
```

- Builds need a full JDK with `javac`, not just a JRE. On this machine the system Java 17 is JRE-only, so use `JAVA_HOME=/opt/android-studio/jbr ./gradlew ...` (verified working).
- `local.properties` is gitignored but mandatory: only `sdk.dir` is required. It no longer contains Supabase values. Never commit this file.
- Dependencies/versions live in `gradle/libs.versions.toml`. AGP 9 uses built-in Kotlin; `android.disallowKotlinSourceSets=false` in `gradle.properties` is required for KSP. Gradle prints a non-fatal "KSP is too old for Kotlin 2.3.20" warning; keep KSP aligned when upgrading Kotlin.
- No CI or ktlint/detekt exists. Unit tests cover intake generation and the reminder scheduler; `lintDebug` is not clean, so before attributing a failure to your change, check whether it already exists on the base revision.

## Architecture

- Layers: `domain/model` + `domain/repository` interfaces; `data/local` (Room entities/DAOs/relations/converters plus DataStore `SessionManager`), `data/mapper`, `data/repository` implementations; `ui/screens/<feature>` Screen+ViewModel pairs; `ui/components`; `di` Hilt modules; `navigation` nested graphs; `notifications` alarm/worker pipeline.
- Keep `domain` pure Kotlin: no Android resources, no `data.*` imports. UI labels for domain enums belong to `ui/components` (for example, `WeekDayLabels.kt`).
- The app is local-only: there is no authentication, remote database, sync layer, or network permission. Do not reintroduce Supabase or account fields.
- New repository: interface in `domain/repository`, implementation in `data/repository`, binding in `di/RepositoryModule`. New entity/DAO: register in `AppDatabase` and provide it through `DatabaseModule`. ViewModels use `@HiltViewModel` and screens obtain them with `hiltViewModel()`.
- Room is version 3 with `exportSchema = false` and `fallbackToDestructiveMigration(dropAllTables = true)`. No migrations exist; bump `AppDatabase.version` for any schema change and expect local data loss.
- Intakes belong to medications (`medicationId`); `scheduleId` is optional and becomes `NULL` when a schedule is deleted. Each intake stores `medicationNameSnapshot`, `doseSnapshot`, and `unitSnapshot` so history survives medication edits. Medications are archived with `deletedAt` instead of being physically deleted.
- Local rows use Int primary keys. Tables are `usuarios`, `profiles`, `medications`, `schedules`, and `intakes`; profiles belong to the single local user.
- WorkManager is Hilt-initialized: the default `WorkManagerInitializer` is removed in `AndroidManifest.xml`, and `PillRemindApp` supplies `HiltWorkerFactory`. New workers need `@HiltWorker` + `@AssistedInject`; do not re-add the default initializer.
- Reminder pipeline: the unique periodic `daily_intake_worker` maintains a rolling 7-day window of intakes, `AlarmScheduler` sets exact alarms (with an inexact fallback), `AlarmReceiver` posts notifications, `BootReceiver` reschedules after boot/update, and `MarkAsTakenReceiver` records the intake. Changes here need on-device verification (exact alarms, notifications, boot behavior, permissions). A local-only known-issues audit may exist at `PillRemind/INCIDENCIAS.md`; it is Git-ignored and must never be committed or published.
- `SessionManager` stores active user/profile IDs, dark mode, and the last intake-generation date in DataStore. Profile selection is the root navigation destination, and the active profile drives home/medication data.

## Docs

- Tracked docs are the root `README.md` and `doc/templates/` (academic project write-up). The README describes the current local-only app; `doc/templates/` is historical and may still describe the removed Supabase version. Verify behavior against code and Gradle config.
- Keep the local `PillRemind/INCIDENCIAS.md` audit out of Git and out of public releases. It is a personal audit document; review it separately from implementation changes.
