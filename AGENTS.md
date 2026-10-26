# AGENTS.md

## Project layout

- Android app only. The Gradle root is `PillRemind/` (single `:app` module); the repo root also contains the academic write-up in `doc/`. Run Gradle commands from `PillRemind/`, not from the repo root.
- Package is `com.andres.pillremind`. Entrypoints: `MainActivity` (Compose UI) and `PillRemindApp` (Hilt/Application setup).
- Docs are Spanish/Galician, and code comments, KDoc, and UI strings are Spanish. Keep identifiers in English and match the surrounding style.

## Commands

```bash
cd PillRemind
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "com.andres.pillremind.ExampleUnitTest"
./gradlew connectedDebugAndroidTest   # device/emulator required
./gradlew lintDebug
```

- Builds need a full JDK with `javac`, not just a JRE. On this machine the system Java 17 is JRE-only, so use `JAVA_HOME=/opt/android-studio/jbr ./gradlew ...` (verified working).
- `local.properties` is gitignored but mandatory: only `sdk.dir` is required. It no longer contains Supabase values. Never commit this file.
- Dependencies/versions live in `gradle/libs.versions.toml`. AGP 9 uses built-in Kotlin; `android.disallowKotlinSourceSets=false` in `gradle.properties` is required for KSP. Gradle prints a non-fatal "KSP is too old for Kotlin 2.3.20" warning; keep KSP aligned when upgrading Kotlin.
- No CI or ktlint/detekt exists. Unit tests cover intake generation and the reminder scheduler; `lintDebug` is not clean, so before attributing a failure to your change, check whether it already exists on the base revision.

## Architecture

- Layers: `domain/model` + `domain/repository` interfaces; `data/local` (Room entities/DAOs/relations/converters plus DataStore `SessionManager`), `data/mapper`, `data/repository` implementations; `ui/screens/<feature>` Screen+ViewModel pairs; `ui/components`; `di` Hilt modules; `navigation` nested graphs; `notifications` alarm/worker pipeline.
- The app is local-only: there is no authentication, remote database, sync layer, or network permission. Do not reintroduce Supabase or account fields.
- New repository: interface in `domain/repository`, implementation in `data/repository`, binding in `di/RepositoryModule`. New entity/DAO: register in `AppDatabase` and provide it through `DatabaseModule`. ViewModels use `@HiltViewModel` and screens obtain them with `hiltViewModel()`.
- Room is version 2 with `exportSchema = false` and `fallbackToDestructiveMigration(dropAllTables = true)`. No migrations exist; bump `AppDatabase.version` for any schema change and expect local data loss.
- Local rows use Int primary keys. Tables are `usuarios`, `profiles`, `medications`, `schedules`, and `intakes`; profiles belong to the single local user.
- WorkManager is Hilt-initialized: the default `WorkManagerInitializer` is removed in `AndroidManifest.xml`, and `PillRemindApp` supplies `HiltWorkerFactory`. New workers need `@HiltWorker` + `@AssistedInject`; do not re-add the default initializer.
- Reminder pipeline: the unique periodic `daily_intake_worker` maintains a rolling 7-day window of intakes, `AlarmScheduler` sets exact alarms (with an inexact fallback), `AlarmReceiver` posts notifications, `BootReceiver` reschedules after boot/update, and `MarkAsTakenReceiver` records the intake. Changes here need on-device verification (exact alarms, notifications, boot behavior, permissions). A local-only known-issues audit may exist at `PillRemind/INCIDENCIAS.md`; it is Git-ignored and must never be committed or published.
- `SessionManager` stores active user/profile IDs, dark mode, and the last intake-generation date in DataStore. Profile selection is the root navigation destination, and the active profile drives home/medication data.

## Docs

- Tracked docs are the root `README.md` and `doc/templates/` (academic project write-up). Do not assume the old architecture docs still exist; verify behavior against code and Gradle config.
- Keep the local `PillRemind/INCIDENCIAS.md` audit out of Git and out of public releases.
