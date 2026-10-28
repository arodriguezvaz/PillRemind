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
