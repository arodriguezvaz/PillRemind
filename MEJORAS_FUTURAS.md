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

## Historial y estadísticas

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

- **Estado:** idea futura.
- **Contexto:** la app es local-only y no tiene sincronización ni copia en nube.
- **Propuesta:** exportar perfiles, medicamentos, horarios y tomas a un archivo
  local, e importarlos con validación de formato y versión.
- **Prioridad:** baja/media; útil para cambiar de dispositivo sin depender de
  backups del sistema.
