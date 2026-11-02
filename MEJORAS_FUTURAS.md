# Mejoras futuras — PillRemind

Este documento recoge mejoras que no forman parte del alcance actual. Los bugs y
la deuda técnica se siguen registrando en el audit local `PillRemind/INCIDENCIAS.md`.

## UI / UX

### UI-04 · Gestionar el resultado del permiso de notificaciones

- **Estado:** aplazada.
- **Contexto:** `MainActivity` solicita `POST_NOTIFICATIONS`, pero el callback de
  `requestPermissionLauncher` está vacío.
- **Problema:** si el usuario deniega el permiso, la app no le explica que no
  recibirá recordatorios visibles y puede pensar que la app está fallando.
- **Propuesta:**
  - mostrar un aviso en Ajustes o un snackbar cuando el permiso esté denegado;
  - ofrecer un acceso directo a los ajustes de notificaciones de la app;
  - no volver a solicitar el permiso automáticamente en cada arranque.
- **Zonas afectadas:** `MainActivity`, `SettingsScreen`, `SettingsViewModel` o un
  componente reutilizable de permisos.
- **Prioridad:** baja/media; no bloquea el flujo actual.

### UI-03 · Completar los enlaces legales de Ajustes

- **Estado:** aplazada.
- **Contexto:** `SettingsScreen` muestra política de privacidad, términos, aviso
  legal, etc., pero las acciones son `/* abrir url */`.
- **Propuesta:** definir las URLs definitivas y abrirlas con el navegador, o
  retirar los enlaces que no vayan a publicarse.
- **Zonas afectadas:** `SettingsScreen` y recursos de texto si los enlaces se
  centralizan.
- **Prioridad:** baja; no afecta al funcionamiento de la app.

### Perfil ampliado con información médica

- **Estado:** implementada.
- **Contexto:** el perfil solo guardaba nombre e imagen.
- **Implementación:** el formulario permite fecha de nacimiento, sexo, grupo
  sanguíneo, alergias, notas médicas y número de tarjeta sanitaria, todos
  opcionales. Los datos se muestran en «Ver perfil» y se incluyen en la copia de
  seguridad local. La base de datos pasa a la versión 6 y los campos nuevos son
  opcionales en el formato de copia.
- **Pendiente:** el botón «Ver adherencia» de «Ver perfil» es un placeholder; la
  pantalla de estadísticas se implementará en una próxima iteración.

## Historial y estadísticas

### Pantalla de adherencia por medicamento

- **Estado:** pendiente.
- **Contexto:** el perfil ya muestra el botón «Ver adherencia» como placeholder.
- **Propuesta:** pantalla con resumen global y estadísticas por medicamento
  (tomas registradas, omitidas y porcentaje) para periodos de 7, 30 y 90 días,
  incluyendo medicamentos archivados.
- **Prioridad:** media.

### Pantalla de historial de tomas

- **Estado:** idea futura.
- **Contexto:** cada `Intake` ya guarda `scheduledAt`, `takenAt` y snapshots de
  nombre, dosis y unidad.
- **Propuesta:** pantalla para consultar tomas por perfil, medicamento y rango de
  fechas, diferenciando tomadas, pendientes y omitidas.
- **Prioridad:** media; el modelo de datos ya está preparado.

## Medicamentos archivados

### Gestión de medicamentos archivados y borrado definitivo

- **Estado:** idea futura.
- **Contexto:** los medicamentos se archivan con `deletedAt` y dejan de aparecer
  en la lista activa, pero todavía no hay forma de consultarlos ni de borrarlos
  definitivamente.
- **Propuesta:** pantalla o filtro de archivados, opción de restaurar y opción de
  borrado definitivo con confirmación.
- **Prioridad:** media.

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
