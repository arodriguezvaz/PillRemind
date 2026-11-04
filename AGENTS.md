# AGENTS.md

## Project layout

- Android app only. The Gradle root is `PillRemind/` (single `:app` module); the repo root also contains the academic write-up in `doc/`. Run Gradle commands from `PillRemind/`, not from the repo root.
- Package is `com.andres.pillremind`. Entrypoints: `MainActivity` (Compose UI) and `PillRemindApp` (Hilt/Application setup).
- Docs are Spanish/Galician, and code comments, KDoc, and UI strings are Spanish. Keep identifiers in English and match the surrounding style.
- PillRemind started as a DAM (Desarrollo de Aplicaciones Multiplataforma) final project and is now a personal project: prioritize data safety, maintainability and real usefulness over academic checklists.

## Android skills

- Load the matching `android-*` skill with the `skill` tool before writing or reviewing code (Kotlin, Compose, architecture, coroutines, Hilt, testing, Gradle); several can apply to the same change.
- The skills live in `.agents/` (installed with autoskills and Git-ignored), so a fresh clone may not have them: when missing, this file and the code are the reference.
- If a skill contradicts this file, this file wins: the app is local-only, with no auth, network or `INTERNET` permission.

## UI strings

- Do not hardcode user-facing text in Compose or other UI code. Add labels, titles, dialog texts, snackbars, error messages and accessibility text to `PillRemind/app/src/main/res/values/strings.xml`, and read them with `stringResource` or `context.getString`.
- ViewModels and other layers should expose `@StringRes` IDs (or an error type mapped in the UI) instead of literal user-facing messages.
- This applies to new code and to any screen or file being modified. When touching a screen with legacy hardcoded strings, move its texts to resources as part of that change.

## Branches

- `master` is the current local-only app and `local/sin-nube` mirrors it. Archived branches keep the old Supabase version: `respaldo/nube`, `respaldo/integridad`, `fix/integridad` and `fix/notificaciones`.
- Check `git branch --show-current` before assuming the architecture. Do not reintroduce Supabase or account fields on `master`/`local/sin-nube`, and do not apply the local-only cleanup on archive branches.
- Branch from an up-to-date `master`, never commit to `master`, and use one branch per issue or feature.
- Names: `<tipo>/<numero>-<descripcion-corta>`; `tipo` is `feat`, `fix`, `refactor`, `perf`, `test`, `docs` or `chore`; the description is lowercase ASCII letters, digits and hyphens (2–5 words) and includes the incident ID when there is one (for example `fix/3-dat-02-intakes`).
- `numero` is the highest existing number of that type plus one; count unmerged branches too (`git branch -a --list 'docs/*'`). Never reuse a number.
- `respaldo/*` and `local/*` are reserved for archived versions.

## Commands

```bash
cd PillRemind
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew testDebugUnitTest --tests "com.andres.pillremind.notifications.ReminderSchedulerTest"
./gradlew connectedDebugAndroidTest   # device/emulator required
./gradlew lintDebug
```

- Builds need a full JDK with `javac`, not just a JRE. On this machine the system Java 17 is JRE-only and `/opt/android-studio/jbr` is Java 25, which breaks Robolectric 4.13 (`Unsupported class file major version 69`). Use a full JDK 17 for `testDebugUnitTest`; `assembleDebug` works with the Studio JBR.
- `local.properties` is Git-ignored but mandatory (`sdk.dir` is enough). Never commit it.
- Versions live in `gradle/libs.versions.toml`. AGP 9 uses built-in Kotlin; `android.disallowKotlinSourceSets=false` in `gradle.properties` is required for KSP, and KSP must stay aligned with the Kotlin version.
- There is no CI, ktlint or detekt. Unit tests cover backup, mappers, repositories, notification handlers and some ViewModels. `lintDebug` is not clean: check whether a failure already exists on the base revision before attributing it to your change.

## Architecture

- Layers: `domain/model` + `domain/repository` interfaces; `data/local` (Room entities/DAOs/relations/converters plus the DataStore `SessionManager`), `data/mapper`, `data/repository` implementations; `ui/screens/<feature>` Screen+ViewModel pairs; `ui/components`; `di` Hilt modules; `navigation` nested graphs; `notifications` alarm/worker pipeline.
- Keep `domain` pure Kotlin: no Android resources, no `data.*` imports. UI labels for domain enums belong to `ui/components` (for example, `WeekDayLabels.kt`).
- The app is local-only: no authentication, remote database, sync layer or network permission. Do not reintroduce Supabase or account fields.
- New repository: interface in `domain/repository`, implementation in `data/repository`, binding in `di/RepositoryModule`. New entity/DAO: register in `AppDatabase`, provide it through `DatabaseModule` and add an index for every foreign-key column (Room warns otherwise). ViewModels use `@HiltViewModel` and screens obtain them with `hiltViewModel()`.
- Room uses `exportSchema = false` and `fallbackToDestructiveMigration(dropAllTables = true)`. There are no migrations: bump `AppDatabase.version` for any schema change and expect local data loss. The app is not in production, so do not write migrations or backwards compatibility (same for `BackupFormat.VERSION`) unless explicitly asked.
- Intakes belong to medications (`medicationId`); `scheduleId` is optional and becomes `NULL` when a schedule is deleted. Each intake stores `medicationNameSnapshot`, `doseSnapshot` and `unitSnapshot` so history survives medication edits. Medications are archived with `deletedAt` instead of being physically deleted.
- WorkManager is Hilt-initialized: the default `WorkManagerInitializer` is removed in `AndroidManifest.xml` and `PillRemindApp` supplies `HiltWorkerFactory`. New workers need `@HiltWorker` + `@AssistedInject`; do not re-add the default initializer.
- Reminder pipeline: the unique periodic `daily_intake_worker` keeps a rolling 7-day window of intakes, `AlarmScheduler` sets exact alarms with an inexact fallback, receivers post notifications and record intakes, and `BootReceiver` reschedules after boot/update. Changes here need on-device verification (exact alarms, notifications, boot behavior, permissions).
- `SessionManager` stores the active user/profile IDs, dark mode, the onboarding flag and the last intake-generation date in DataStore. Profile selection is the root navigation destination, and the active profile drives home/medication data.

## Docs

- Tracked docs are the root `README.md`, `MEJORAS_FUTURAS.md` (deferred improvements) and `doc/templates/` (academic write-up, historical: it may still describe the removed Supabase version). Verify behavior against code and Gradle config.
- `PillRemind/INCIDENCIAS.md` is a local, Git-ignored personal audit: never commit or publish it, and review it separately from implementation changes.
