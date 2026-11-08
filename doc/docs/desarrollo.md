# Guía de desarrollo

Documentación técnica de PillRemind: cómo compilar el proyecto, cómo está
organizado el código, cómo se publican las versiones y cómo funciona el sistema de
actualizaciones.

## Requisitos

- Android Studio con el SDK de Android instalado.
- `PillRemind/local.properties` con la ruta al SDK (`sdk.dir`). El archivo está en
  `.gitignore` y no se sube al repositorio.
- Para los tests unitarios, un **JDK 17 completo** (con `javac`). Con el JBR de
  Android Studio (Java 25) Robolectric 4.13 falla con
  `Unsupported class file major version 69`.

Las versiones de dependencias están en `PillRemind/gradle/libs.versions.toml`.

## Compilación y pruebas

Todos los comandos se ejecutan desde `PillRemind/`:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest   # requiere dispositivo o emulador
./gradlew lintDebug
```

`lintDebug` no está limpio en todas las revisiones: comprueba si un fallo ya existía
antes de atribuirlo a un cambio.

## Arquitectura

La aplicación sigue una separación por capas:

- `domain/model` y `domain/repository`: modelos y contratos en Kotlin puro, sin
  dependencias de Android.
- `data/local`: base de datos Room (entidades, DAOs, relaciones y conversores) y
  `SessionManager` sobre DataStore.
- `data/mapper`, `data/repository`: mapeadores y implementaciones de los contratos.
- `ui/screens/<feature>`: pantallas Compose con su ViewModel (`@HiltViewModel`).
- `ui/components`, `navigation`, `di` y `notifications`.

La aplicación es **local-only**: no hay autenticación, base de datos remota ni
sincronización. La única petición de red es la comprobación de actualizaciones
(`data/remote/GitHubUpdateDataSource`).

## Base de datos y migraciones

- Room exporta los esquemas a `app/schemas` (`exportSchema = true`).
- La versión se declara en `APP_DATABASE_VERSION` (`AppDatabase.kt`).
- Cada cambio de esquema necesita una `@AutoMigration` o una `Migration` manual en
  `DatabaseMigrations`, el JSON del esquema en `app/schemas` y pruebas de migración.
- Las pruebas están en `AppDatabaseMigrationTest` (Robolectric) y
  `AppDatabaseMigrationInstrumentedTest` (dispositivo).
- No se usa `fallbackToDestructiveMigration` desde la versión 6.

## Copias de seguridad

- El formato se define en `BackupFormat` y actualmente es la **versión 2**.
- Los DTO antiguos están congelados en `data/backup/legacy` y se importan mediante
  migradores; cada versión soportada tiene su prueba.
- La importación sustituye todos los datos actuales.
- Solo se incrementa la versión del formato si cambia el JSON exportado.

## Recordatorios y notificaciones

- WorkManager se inicializa con Hilt: el inicializador por defecto está desactivado
  en `AndroidManifest.xml` y `PillRemindApp` aporta `HiltWorkerFactory`.
- El worker periódico `daily_intake_worker` mantiene una ventana móvil de 7 días de
  tomas.
- `AlarmScheduler` programa alarmas exactas con respaldo inexacto si el permiso no
  está disponible.
- `AlarmReceiver` publica la notificación y `MarkAsTakenReceiver` permite marcar la
  toma desde la propia notificación.
- `BootReceiver` reprograma las alarmas tras reiniciar o actualizar la app.
- Cualquier cambio en esta cadena debe probarse en un dispositivo real: alarmas
  exactas, notificaciones, reinicio y permisos.

## Publicación

El APK de release se compila, firma y publica con GitHub Actions
(`.github/workflows/release.yml`). Para lanzar una versión:

```bash
git tag v1.0.0
git push origin v1.0.0
```

El workflow toma la versión de la etiqueta, usa el número de ejecución como
`versionCode` y adjunta `PillRemind-1.0.0.apk` y `update.json` a la release. También
se puede ejecutar a mano desde la pestaña Actions: en ese caso solo sube el APK como
artefacto, sin crear release.

La firma necesita cuatro secretos en el repositorio (Settings → Secrets and
variables → Actions): `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` y
`KEY_PASSWORD`. En local se puede crear `PillRemind/keystore.properties` con
`storeFile`, `storePassword`, `keyAlias` y `keyPassword`; tanto el archivo como el
keystore están en `.gitignore`.

Si el token del repositorio es de solo lectura, hay que activar «Read and write
permissions» en Settings → Actions → General.

## Actualizaciones

- La app consulta como máximo **una vez al día** el `update.json` de la última
  release (`UpdateViewModel`). Si falla la red, se mantiene la información cacheada y
  la aplicación no se bloquea.
- `UpdateGate` decide qué mostrar:
  - **Opcional**: diálogo «Nueva versión disponible» que se puede posponer.
  - **Obligatoria**: pantalla que solo permite actualizar o salir.
- El botón de actualizar abre la URL del APK en el navegador; la instalación la
  confirma el instalador del sistema.
- `update.json` incluye `versionCode`, `versionName`, `minimumVersionCode` y las URLs
  del APK y de la release.
- La variable `UPDATE_REQUIRED` (Settings → Secrets and variables → Actions →
  Variables) a `true` hace que la release obligue a actualizar. Si no existe o vale
  `false`, el aviso es opcional.
