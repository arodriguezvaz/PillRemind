# Mejoras futuras — PillRemind

Este documento recoge mejoras que no forman parte del alcance actual. Los bugs y
la deuda técnica se siguen registrando en el audit local `PillRemind/INCIDENCIAS.md`.

## UI / UX

### UI-04 · Gestionar el resultado del permiso de notificaciones

- **Estado:** implementada.
- **Contexto:** `MainActivity` solicitaba `POST_NOTIFICATIONS` al arrancar y el
  callback de `requestPermissionLauncher` estaba vacío.
- **Implementación:** se eliminó la petición automática. La bienvenida de primer
  arranque solicita las notificaciones y, si el usuario las deniega, le explica
  que no recibirá recordatorios y le ofrece abrir los ajustes del sistema.
  Ajustes muestra el estado de notificaciones y alarmas exactas con accesos
  directos para corregirlos.
- **Zonas afectadas:** `MainActivity`, `OnboardingScreen`, `OnboardingViewModel`,
  `ReminderPermissions`, `SettingsScreen`.

### Bienvenida de primer arranque

- **Estado:** implementada.
- **Contexto:** la app abría directamente la selección de perfiles y pedía las
  notificaciones sin explicar para qué se usaban.
- **Implementación:** pantalla única con la marca, las funciones principales y
  dos tarjetas de permiso con su estado. Las notificaciones son obligatorias;
  las alarmas exactas también, salvo que el dispositivo no permita concederlas,
  en cuyo caso se ofrece continuar con un aviso de que los avisos pueden
  retrasarse. El flag se guarda en DataStore y la bienvenida no vuelve a
  aparecer.
- **Zonas afectadas:** `SessionManager`, `MainActivity`, `OnboardingScreen`,
  `OnboardingViewModel` y `ReminderPermissions`.

### UI-03 · Completar los enlaces legales de Ajustes

- **Estado:** implementada.
- **Contexto:** `SettingsScreen` mostraba política de privacidad, términos y
  aviso legal, pero las acciones eran `/* abrir url */`.
- **Implementación:** la política de privacidad y el aviso legal abren un diálogo
  local con el texto correspondiente, por lo que funcionan sin conexión. Se
  retiraron los términos y condiciones, que no aportaban nada a una app local sin
  cuentas ni compras.
- **Zonas afectadas:** `SettingsScreen` y recursos de texto.

### Perfil ampliado con información médica

- **Estado:** implementada.
- **Contexto:** el perfil solo guardaba nombre e imagen.
- **Implementación:** el formulario permite fecha de nacimiento, sexo, grupo
  sanguíneo, alergias, notas médicas y número de tarjeta sanitaria, todos
  opcionales. Los datos se muestran en «Ver perfil» y se incluyen en la copia de
  seguridad local. La base de datos pasa a la versión 6 y los campos nuevos son
  opcionales en el formato de copia.

## Historial y estadísticas

### Pantalla de estadísticas

- **Estado:** implementada.
- **Contexto:** el perfil muestra la fila «Ver estadísticas» y la pantalla de
  Inicio incluye un resumen semanal cuando hay tomas evaluadas.
- **Implementación:** ruta de detalle `statistics` con periodos de 7, 30 y 90
  días, resumen de adherencia, gráfico de barras de los últimos 7 días,
  gráfico de líneas de adherencia semanal para 30 y 90 días y desglose por
  medicamento (incluidos los archivados). El porcentaje no penaliza las tomas
  pendientes y usa el mismo margen de 15 minutos que Inicio. La ficha de cada
  medicamento abre la misma pantalla filtrada por ese medicamento. El
  historial de tomas tiene una pantalla propia.
- **Pendiente:** exportar estadísticas y comparar perfiles.

### Historial de tomas

- **Estado:** implementada.
- **Contexto:** cada `Intake` guarda `scheduledAt`, `takenAt` y snapshots de
  nombre, dosis y unidad.
- **Implementación:** ruta de detalle `history` con búsqueda por nombre de
  medicamento, filtros por estado, medicamento, medicamentos archivados y rango
  de fechas personalizado (por defecto, últimos 30 días), y lista agrupada por
  día de más reciente a más antigua. Se abre desde la sección «Historial» del
  perfil y desde la ficha de cada medicamento, con ese medicamento
  preseleccionado. El bloque de historial ya no se muestra dentro de
  Estadísticas.
- **Pendiente:** exportar el historial filtrado y guardar filtros favoritos.
- **Prioridad:** baja.

## Medicamentos archivados

### Gestión de medicamentos archivados y borrado definitivo

- **Estado:** implementada.
- **Contexto:** los medicamentos se archivan con `deletedAt` y dejan de aparecer
  en la lista activa. El archivado conserva la pauta para poder recuperarlos.
- **Implementación:** el perfil incluye la fila «Medicamentos archivados» dentro
  de Seguimiento. La lista abre una ficha de solo lectura con el aviso de archivo,
  el tratamiento, la pauta conservada y acceso a sus estadísticas. Desde ahí se
  puede desarchivar (se regeneran las tomas y alarmas) o eliminar definitivamente
  con confirmación, borrando también la imagen y el historial.
- **Pendiente:** buscador y filtros en la lista de archivados.
- **Prioridad:** baja.

## Datos locales

### Exportar e importar datos

- **Estado:** implementada.
- **Contexto:** la app es local-only y no tiene sincronización ni copia en nube.
- **Implementación:** desde Ajustes se puede exportar e importar un archivo
  `.pmbackup` (contenedor con cabecera propia y un ZIP interno con `backup.json`
  e imágenes). Incluye perfiles, medicamentos, horarios, historial y fotos. La
  importación reemplaza todos los datos actuales.
- **Cifrado:** opcional con contraseña (AES-256-GCM con clave derivada por PBKDF2).
  Si se olvida la contraseña, la copia no se puede recuperar.
- **Compatibilidad:** el formato incluye una versión; las copias de versiones no
  soportadas se rechazan con un mensaje claro.
