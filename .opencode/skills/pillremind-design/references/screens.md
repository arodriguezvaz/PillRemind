# Estructura de pantallas

Patrones de layout que ya existen. Úsalos como plantilla antes de inventar una
estructura nueva. Para añadir una pestaña, sigue además `new-tab.md`.

## Mapa de navegación

```text
RootNavigationGraph
├── profileSelectionGraph
│   ├── ProfilesScreen.ProfileSelectionScreen   ("profiles")
│   ├── ProfilesScreen.ProfileCreateScreen      ("profile/create")
│   └── ProfilesScreen.SettingsScreen           ("settings")
└── Graph.MAIN -> MainScreen
    ├── BottomBarScreens.Home                   ("home")
    ├── BottomBarScreens.Medications            ("medications")
    │   └── medicationNavGraph
    │       ├── MedicationSeeScreen             ("medication/see/{medicationId}")
    │       └── MedicationCreateScreen          ("medication/create")
    └── BottomBarScreens.Profile                ("profile/see")
```

El grafo raíz está en `navigation/RootNavGraph.kt`; el de la barra inferior, en
`navigation/MainNavGraph.kt`.

## Contenedor principal

### `MainScreen` (`screens/main/MainScreen.kt`)

- `Scaffold` con:
  - `topBar = MainTopBar`, alimentada por `TopBarState`.
  - `bottomBar = MainBottomBar` (Home, Medications, Profile).
  - FAB solo en Home y Medicamentos, con `AppFab`; su `onClick` siempre navega a
    `MedicationScreens.MedicationCreateScreen`.
- `topLevelRoutes` contiene las rutas de Home, Medications y Profile. Las rutas
  que no están en ese conjunto muestran flecha atrás si hay historial.
- Pasa `paddingValues` a `MainNavigationGraph`, que aplica
  `Modifier.padding(paddingValues)` al `NavHost`.

### `MainTopBar` (`screens/main/MainTopBar.kt`)

- `TopAppBar` con fondo `background`.
- Título `titleLarge`; logo `PILL` (Bold) + `REMIND` (Normal) si
  `showLogo = true`.
- `navigationAction` opcional sustituye al botón de retroceso; si es `null`, se
  usa la flecha cuando la ruta no es de primer nivel y hay historial.
- `actions` muestra iconos a la derecha.

### `MainBottomBar` (`screens/main/MainBottomBar.kt`)

- Solo se muestra si la ruta actual coincide exactamente con una ruta de
  `BottomBarScreens`.
- Seleccionado: icono y texto `primary`; no seleccionado: `onSurfaceVariant`.
- Indicador `primary` con alpha 0.12.
- Navegación con `popUpTo(startDestination)`, `saveState`, `launchSingleTop` y
  `restoreState`.

## Home

`HomeScreen.kt`

```text
Column(fillMaxSize + padding(AppSpacing.md))
├── LazyColumn(
│     contentPadding = PaddingValues(vertical = AppSpacing.sm),
│     verticalArrangement = spacedBy(AppSpacing.sm))
│   └── IntakeCard(...)
└── o BlankCard(home_empty_today) centrada
```

- Título de la top bar: `home_title_today` con la fecha formateada en español;
  `showLogo = true`.
- Usa `collectAsState()` (código heredado). En código nuevo, usa
  `collectAsStateWithLifecycle()`.

## Medicamentos

`MedicationsScreen.kt`

```text
Column(fillMaxSize + padding(AppSpacing.md))
├── AppSearchField(bottom = AppSpacing.sm)
└── LazyColumn(vertical = AppSpacing.sm, spacedBy = AppSpacing.sm)
    └── MedicationCard(...)
```

- Estado vacío: `BlankCard(medication_empty)` o
  `BlankCard(medication_search_no_results, query)`.
- Cada item lleva `key = { it.id }`.

## Ficha de medicamento

`MedicationScreen.kt` con `MedicationMode.Create / Edit / View`.

- Raíz de cada modo:
  `Column(fillMaxSize + padding(AppSpacing.md) + verticalScroll)`.
- Orden:
  1. `AppPhotoPicker` con `bottom = AppSpacing.xl`.
  2. `AppFormSection(medication_data_section)` con los campos de datos.
  3. `AppFormSection(medication_schedule_section)` + `ScheduleEditor`.
  4. `AppFormActions` al final (`guardar`; en View, `editar` + archivar).
- En `View`: `AppInfoCard` + `AppInfoRow` para datos y pauta.
- La acción secundaria de `AppFormActions` sirve para archivar o eliminar; en
  `ProfileScreen` se marca con `secondaryContentColor = error`.
- `LaunchedEffect(currentMode, topBarTitle)` actualiza la top bar; en modo Edit
  muestra `navigationAction` para volver a View y `BackHandler` hace lo mismo.

## Perfiles

`ProfilesScreen.kt`

- `Scaffold` propio con `MainTopBar` (acción de ajustes) y FAB de añadir perfil
  con `AppFab`.
- Lista:
  `LazyColumn(contentPadding = horizontal 16dp + vertical 12dp, spacedBy 12dp)`.
- `PerfilCard` por perfil; estado vacío `BlankCard(profiles_empty)`.
- Es el patrón de referencia para una pantalla con `Scaffold` propio y FAB
  local.

## Ficha de perfil

`ProfileScreen.kt` con `ProfileMode.Create / Edit / View`.

- `ProfileForm`:
  `Column(fillMaxSize + verticalScroll + imePadding + padding(AppSpacing.md))`
  con `spacedBy(AppSpacing.lg)`.
- Orden:
  1. `AppPhotoPicker` centrado.
  2. `AppFormSection(profile_personal_data)`.
  3. `AppFormSection(profile_medical_info)`.
  4. `AppFormSection(profile_admin_data)`.
  5. `AppFormActions` al final (`guardar` o `editar` + eliminar).
- En `View`: `AppInfoCard` + `AppInfoRow` para las tres secciones.
- El diálogo de eliminación usa `AlertDialog` con textos de `strings.xml`.

## Ajustes

`SettingsScreen.kt`

- `Scaffold` con `MainTopBar` y `SnackbarHost`.
- Column con scroll:
  `padding(horizontal = AppSpacing.md)` y `spacedBy(AppSpacing.sm)`.
- Bloques:
  1. Apariencia: `SettingsCard` sin título + `SettingsToggleItem` (modo oscuro).
  2. Recordatorios: `SettingsCard` con notificaciones y alarmas exactas.
  3. Copia de seguridad: descripción, `SettingsLinkItem` de exportar/importar,
     indicador de progreso y aviso de privacidad.
  4. Legal: `SettingsLinkItem` de privacidad y aviso legal.
- Los huecos de 10dp entre descripción y enlaces son una excepción puntual.

## Onboarding

`OnboardingScreen.kt`

- `OnboardingRoute` obtiene el ViewModel; `OnboardingScreen` es stateless
  (`OnboardingUiState` + callbacks), lo que permite previsualizarla.
- `Scaffold` propio, sin `MainTopBar` ni `MainBottomBar`.
- Columna central con scroll, `horizontal = AppSpacing.md` y
  `vertical = AppSpacing.lg`, separada con `AppSpacing.lg`.
- `BrandHeader` + título `titleMedium` Bold en `primary` + descripción
  `bodyMedium` en `onSurfaceVariant`.
- `PermissionCard` por permiso, con `PermissionStatusChip`.
- Acciones inferiores: `AppButton` `Large` y avisos en `bodySmall`.

## Reglas al añadir una pantalla

1. Si es una pestaña de primer nivel, usa el `Scaffold` de `MainScreen` y sigue
   `new-tab.md`; no añadas otro `Scaffold`.
2. Si la pantalla vive fuera de `MainScreen` y necesita su propio `Scaffold`
   (por ejemplo, un FAB local), reutiliza `MainTopBar` y pásale
   `topLevelRoutes`.
3. Usa `AppSpacing` para el margen y las separaciones.
4. Usa `BlankCard` para el estado vacío, centrada.
5. Si hay formulario, divide con `AppFormSection`, cierra con `AppFormActions`
   y no mezcles lectura y edición.
6. Añade los textos a `strings.xml` y el `@Preview` del componente nuevo.
7. En código nuevo, consume estado con `collectAsStateWithLifecycle()`.
8. Comprueba claro/oscuro y texto grande antes de terminar.
