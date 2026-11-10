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

- `domain/model`, `domain/repository` y `domain/parsing`: modelos, contratos y
  lógica de extracción en Kotlin puro, sin dependencias de Android.
- `data/local`: base de datos Room (entidades, DAOs, relaciones y conversores) y
  `SessionManager` sobre DataStore.
- `data/ocr` y `data/scan`: OCR y lectura de códigos con ML Kit.
- `data/mapper`, `data/repository`: mapeadores e implementaciones de los contratos.
- `ui/screens/<feature>`: pantallas Compose con su ViewModel (`@HiltViewModel`).
- `ui/components`, `navigation`, `di` y `notifications`.

Los datos son **local-only**: no hay autenticación, base de datos remota ni
sincronización. La app solo hace dos peticiones de red: la comprobación de
actualizaciones (`data/remote/GitHubUpdateDataSource`) y la consulta a CIMA
(`data/remote/CimaDataSource`) cuando el usuario escanea y acepta el
consentimiento. CIMA solo recibe el CN.

## Escaneo de medicamentos

El flujo está en `MedicationScanViewModel` y `MedicationScanFlow`:

1. El usuario añade una foto de la caja y/o del código, o escribe el CN.
2. `MlKitBarcodeEngine` lee EAN-13, DataMatrix y QR. `MedicineCodeParser` extrae
   el CN de los formatos OSS (`847000...`) y GS1 (`712`/NTIN); si no hay código
   legible, `CnParser` busca candidatos en el OCR de la foto del código.
3. `MlKitOcrEngine` lee la caja y devuelve un `OcrResult` con las líneas y sus
   cajas. `BoxTextParser` puntúa el nombre por tamaño y posición, extrae
   concentración, forma y toma, separa presentación de dosis y marca
   `nameConfident`; `MedicationDraftMapper` construye un `MedicationDraft`
   efímero.
4. Antes de la primera consulta a CIMA se pide consentimiento
   (`SessionManager.cimaConsent`). Solo se envía el CN.
5. `CimaDataSource` consulta la API pública. Si el CN no está catalogado
   (204/404) o falla la red, se usa el borrador del OCR; si el usuario rechaza el
   consentimiento, no se consulta.
6. La pantalla de revisión permite corregir los datos antes de pasarlos al
   formulario. El CN y el borrador no se guardan en Room ni en las copias de
   seguridad.

El OCR es heurístico: si el nombre no destaca con claridad, `BoxTextParser` deja
`name` vacío y devuelve candidatos; la revisión los muestra para que el usuario
elija. Un volumen suelto (`120 ml`) se trata como presentación, no como dosis. En
debug, las líneas OCR y sus cajas se registran en logcat con la etiqueta
`PillRemindOcr`. La revisión manual es obligatoria. Las pruebas del flujo están en
`BoxTextParserTest`, `CnParserTest`, `MedicationDraftMapperTest`,
`CimaDataSourceTest` y `MedicationLookupRepositoryImplTest`; los assets de códigos
para las pruebas instrumentadas están en `app/src/androidTest/assets/barcodes`.

El detalle completo está en [Escaneo de medicamentos](escaneo.md).

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

## Estadísticas de descargas

El badge de descargas del README se calcula con la API de GitHub Releases y solo
suma los assets `*.apk`. No cuenta los `update.json`, que la app descarga para
comprobar actualizaciones.

Para ver el desglose completo:

```bash
python3 scripts/release_stats.py
```

El script usa solo la biblioteca estándar de Python y no necesita token; si
existe `GITHUB_TOKEN` o `GH_TOKEN` en el entorno, lo usa para ampliar el límite
de peticiones. Con `--json` genera el payload que consume
`shields.io/endpoint`.

El workflow `.github/workflows/stats.yml` publica `estadisticas.json` en la rama
`stats` a diario y a mano desde Actions. Las releases creadas por
`release.yml` no disparan el evento `release` (GitHub no encadena workflows
iniciados con `GITHUB_TOKEN`), así que el contador se pone al día en la
siguiente ejecución diaria. El historial de la rama sirve como serie temporal.

Qué no mide:

- **Instalaciones únicas**: GitHub no identifica dispositivos.
- **Actualizaciones**: cada actualización vuelve a descargar el APK y suma.
- **APK compartidos fuera de GitHub** (por ejemplo, por mensajería).
- **Tus propias descargas de prueba**.

Como aproximación de uso, el asset `update.json` de la última release cuenta las
comprobaciones que hace la app (como máximo una cada 24 h por instalación). No son
usuarios únicos, pero sí un indicador de dispositivos activos.

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
