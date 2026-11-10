# Escaneo de medicamentos: OCR, códigos y CIMA

Este documento explica cómo funciona el escaneo de medicamentos de PillRemind: qué
se lee de las fotos, cómo se obtiene el CN, cuándo se consulta CIMA, cómo se
construye la ficha que ve el usuario y qué limitaciones tiene.

El objetivo no es identificar el medicamento con total certeza, sino **rellenar la
ficha con una propuesta editable** y que el usuario la revise antes de guardarla.
La app no es un producto sanitario y no sustituye al prospecto ni al profesional
sanitario.

## 1. Flujo general

```
Fotos (caja / código)  ─┐
CN escrito a mano     ─┼─> Lectura de código ──> CN ──> ¿Consentimiento? ──> CIMA
                       │                                      │              │
                       └─> OCR de la caja ──> BoxFields        │              ├─> ficha oficial
                                                               │              │
                                                               └─> no / error └─> borrador OCR
                                                                                     │
                                                                          Pantalla de revisión
                                                                                     │
                                                                              Formulario final
```

El estado del flujo vive en `MedicationScanViewModel`:

- `ScanStage.PHOTOS`: selección de fotos o CN manual.
- `ScanStage.ANALYZING`: lectura de código, OCR, consentimiento y consulta.
- `ScanStage.RESULT`: revisión del borrador.

El CN y el borrador son **efímeros**. No se guardan en Room ni en las copias de
seguridad: solo pasan al formulario los valores que el usuario acepta.

## 2. Entrada de fotos y preparación

La pantalla `MedicationScanFlow` permite dos fotos opcionales:

- **Caja**: cara frontal, donde suelen estar el nombre, la concentración y la
  forma farmacéutica.
- **Código**: CN impreso, EAN-13 o DataMatrix.

También se puede escribir el CN a mano. Con una sola foto se puede analizar; con
las dos el resultado es más fiable.

`BitmapLoader` prepara cada imagen antes de pasarla a ML Kit:

- reduce el lado mayor a **2048 px** para no gastar memoria;
- aplica la orientación EXIF;
- devuelve siempre un bitmap software.

## 3. Lectura de códigos

`MlKitBarcodeEngine` usa ML Kit Barcode Scanning con los formatos:

- **EAN-13**: el que usan muchos envases españoles.
- **DataMatrix**: habitual en envases serializados.
- **QR**: se acepta como extra.

Si la primera lectura no encuentra nada, repite el proceso con la imagen
invertida, para códigos claros sobre fondo oscuro. Cada intento tiene un tiempo
máximo de **20 segundos**.

`MedicineCodeParser` interpreta el contenido:

- EAN-13 con prefijo **`847000`**: los seis dígitos siguientes son el CN.
- DataMatrix GS1 con campo **`(712)`**: se toman los seis primeros dígitos del
  NHRN.
- NTIN que empieza por **`0847000`**: se extrae el CN de los dígitos centrales.
- Se valida el dígito de control del EAN-13 antes de aceptar el código.

Si el lector de códigos no obtiene nada, se hace OCR de la foto del código y
`CnParser` busca candidatos en el texto.

## 4. Extracción del CN

`CnParser` prioriza estas formas:

1. CN etiquetado: `C.N. 215936.5`, `CN 663526.1`, etc.
2. Seis dígitos con separador y dígito de control: `215936.5`.
3. Siete dígitos seguidos: se usan los seis primeros.
4. Bloques de seis dígitos sueltos.

El CN escrito a mano se normaliza: se aceptan seis o siete dígitos y se descarta
el dígito de control. Cada fuente aporta como máximo tres candidatos y el flujo
prueba hasta cinco en total, en orden, hasta que CIMA encuentra uno.

## 5. OCR de la caja

`MlKitOcrEngine` usa ML Kit Text Recognition (latín) sobre la foto de la caja.
También tiene un tiempo máximo de 20 segundos. Devuelve un `OcrResult` con el
texto completo y las líneas con su caja normalizada (0..1).

En builds de debug, el motor registra en logcat (etiqueta `PillRemindOcr`) cada
línea reconocida con su caja. Sirve para comprobar qué leyó ML Kit y ajustar los
umbrales; en release no se registra.

El texto se pasa a `BoxTextParser`, que trabaja con líneas:

1. **Filtra ruido**: lote, caducidad, prospecto, laboratorio, direcciones,
   teléfonos, textos largos, líneas con muchos dígitos, etc.
2. **Busca la concentración** con una expresión regular que acepta `mg`, `g`,
   `mcg`, `ml`, `UI` y `%`.
3. **Detecta la forma farmacéutica** (comprimidos, cápsulas, sobres, parches,
   pulverización, solución, etc.).
4. **Detecta la toma explícita** cuando aparece algo como `1 comprimido` o
   `2 cápsulas`.
5. **Elige el nombre** entre las líneas candidatas. Si el OCR conserva la
   geometría, puntúa cada línea por altura, área y posición central; penaliza el
   texto pequeño de la franja superior (marcas y logos), da un bonus a `®` y
   filtra descriptores como `CANNABIDIOL` o `SIN SABOR`. Devuelve hasta tres
   candidatos.
6. **Decide la confianza**: si el primer candidato destaca claramente sobre el
   segundo, el nombre se rellena; si no, queda vacío y la revisión ofrece los
   candidatos.

`BoxTextParser` devuelve un `BoxFields` con:

- `name`, `nameCandidates`, `nameConfident`
- `strengthDoseText`, `strengthUnit`, `strengthText`
- `presentationText`
- `form`
- `intakeDoseText`, `intakeUnit`

### Nombre y confianza

La elección no es semántica: se apoya en el tamaño y la posición del texto, no en
entender qué es una marca o un principio activo. En una caja con una marca
pequeña arriba y el nombre del producto grande debajo, la geometría suele elegir
bien el producto (`MABOdol` frente a `EFECTO`), pero puede fallar si el OCR no lee
el nombre grande o si dos líneas tienen un tamaño parecido.

Si el mejor candidato está en la franja superior, aunque sea grande, tampoco se da
por seguro: puede ser la marca y no el producto. En ese caso el nombre no se
rellena a ciegas: la pantalla de revisión muestra hasta tres candidatos y el
usuario elige o escribe el correcto. Las líneas sin caja se conservan como
candidatos, pero no puntúan por geometría. La revisión manual sigue siendo
obligatoria.

## 6. Construcción del borrador

`MedicationDraftMapper` convierte el resultado en un `MedicationDraft`:

- **Desde CIMA**: usa el nombre oficial, la dosis, la forma, la presentación y el
  CN. El nombre se acorta antes de la forma farmacéutica (`shortName`).
- **Desde OCR**: usa los campos de la caja, marca el medicamento como no
  catalogado y no inventa datos que no aparezcan.

Reglas de dosis:

- Formas sólidas (comprimidos, cápsulas, sobres...): se propone `1` unidad.
- Si hay una toma explícita (`N gotas`, `1 comprimido`...), se usa esa toma.
- Si solo hay una concentración con `/` (`200 mg/5 ml`), no se usa como dosis:
  describe una concentración, no una cantidad.
- Un volumen suelto (`120 ml`) sin `/` ni toma explícita se guarda como
  presentación, no como dosis.
- Un porcentaje (`10%`) es una concentración, no una dosis.
- Un recuento como `20 comprimidos` sin contexto de pauta se interpreta como
  tamaño del envase; la toma explícita exige contexto (`cada`, `al día`,
  `tomar`, `dosis`...).
- Si no hay toma ni concentración utilizable, la dosis queda vacía y el usuario
  la escribe en la revisión.

La pantalla de revisión permite editar nombre, dosis y unidad. Si
`nameConfident` es `false`, el campo de nombre aparece vacío y se muestran hasta
tres `FilterChip` con `nameCandidates`; el usuario elige uno o escribe el suyo.
Hasta que el nombre, la dosis y la unidad son válidos no se puede pasar al
formulario.

## 7. Consulta a CIMA

`CimaDataSource` consulta la API pública de la AEMPS:

```
GET https://cima.aemps.es/cima/rest/medicamento?cn=<CN>
Accept: application/json
User-Agent: PillRemind
```

Comportamiento:

- **200**: se parsea el JSON y se construye `CimaMedication`.
- **204 o 404**: el CN no está catalogado; se devuelve `null`.
- **Otro código**: se lanza `IOException` y el flujo cae al borrador del OCR.
- Tiempo máximo: **7 segundos**.
- Cuerpo máximo: **128 KB**.

Antes de la primera consulta se muestra un diálogo de consentimiento
(`scan_cn_consent_*`). Si el usuario acepta, se guarda la preferencia en
`SessionManager.cimaConsent` y no se vuelve a preguntar. Si rechaza, no se envía
nada y se usa el OCR o el formulario manual.

La consulta solo envía el **CN**. No se envían perfiles, medicamentos, tomas ni
fotos. La AEMPS recibe la dirección IP y datos técnicos de la conexión, como en
cualquier petición HTTP. El CN no se persiste y el resultado se descarta al
terminar la revisión.

## 8. Estados y errores

`ScanUiState` distingue el estado de cada paso (`PENDING`, `RUNNING`, `DONE`,
`FAILED`, `SKIPPED`) y muestra un aviso al terminar:

- `CIMA_FOUND`: se encontró una ficha oficial.
- `CIMA_NOT_FOUND`: el CN no está catalogado; se usa el OCR.
- `CIMA_ERROR`: fallo de red o de la API; se usa el OCR.
- `CIMA_NOT_CONSULTED`: el usuario rechazó el consentimiento.
- `CN_NOT_DETECTED`: no se encontró un CN.
- `CODE_UNREADABLE`: se intentó leer el código pero no se pudo.
- `NOTHING`: no se pudo leer nada.

Un fallo de red nunca bloquea el flujo: siempre queda el OCR o el formulario
manual. El análisis se puede cancelar volviendo a las fotos.

## 9. Persistencia y privacidad

- Las fotos se procesan en el dispositivo con ML Kit; no se suben a ningún
  servidor.
- El escaneo usa los modelos de ML Kit servidos por Google Play Services. En un
  dispositivo sin Play Services puede no estar disponible; el resto de la app
  sigue funcionando y la ficha se puede rellenar a mano.
- El CN y el borrador viven en memoria durante el flujo.
- Solo se guardan en Room los campos que el usuario acepta en el formulario.
- No hay cuenta, sincronización ni servidor propio.
- La otra conexión de red de la app es la comprobación de actualizaciones contra
  GitHub Releases; no envía datos de salud.

## 10. Pruebas

Pruebas unitarias:

- `BoxTextParserTest`: nombre, candidatos y confianza con geometría; presentación
  frente a dosis; concentración, forma y toma a partir de texto OCR.
- `CnParserTest`: CN etiquetado, con separador, de seis/siete dígitos y códigos.
- `MedicationDraftMapperTest`: reglas de dosis, presentación, concentración
  compuesta, porcentaje y nombre corto.
- `MedicationScanViewModelTest`: los candidatos y la presentación llegan a la
  pantalla de revisión.
- `CimaDataSourceTest`: parseo del JSON y respuestas 204/404.
- `MedicationLookupRepositoryImplTest`: fallback y errores de la consulta.

Pruebas instrumentadas y material de apoyo:

- Assets de códigos en `app/src/androidTest/assets/barcodes`.
- `CodeScanDemoActivity` (solo debug) permite probar el lector con esos assets y
  ver el resultado sin pasar por el flujo completo.

## 11. Referencias

- API pública de CIMA (AEMPS): <https://cima.aemps.es/cima/rest/medicamento>
- ML Kit Barcode Scanning: <https://developers.google.com/ml-kit/vision/barcode-scanning/android>
- ML Kit Text Recognition: <https://developers.google.com/ml-kit/vision/text-recognition/v2/android>
- Guía de desarrollo de PillRemind: [desarrollo.md](desarrollo.md)
- Manual de uso: [manual-uso.md](manual-uso.md)
