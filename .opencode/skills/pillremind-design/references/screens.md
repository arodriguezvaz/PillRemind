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
    ├── BottomBarScreens.Profile                ("profile/see")
    ├── StatisticsScreens.Statistics            ("statistics")
    ├── StatisticsScreens.MedicationStatistics  ("statistics/medication/{medicationId}")
    └── HistoryScreens.History                  ("history?medicationId={medicationId}")
```

El grafo raíz está en `navigation/RootNavGraph.kt`; el de la barra inferior, en
`navigation/MainNavGraph.kt`.

## Contenedor principal

### `MainScreen` (`screens/main/MainScreen.kt`)

- `Scaffold` con:
  - `topBar = MainTopBar`, alimentada por `TopBarState`. El patrón correcto es
    derivarlo de la ruta actual (`currentBackStackEntryAsState()` + `when`); el
    código antiguo todavía puede empujarlo con `onTopBarChange`.
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

## Top bar

La fuente de verdad es la ruta actual. `MainScreen` deriva `TopBarState` con
`currentBackStackEntryAsState()` y un `when` de rutas. Las pestañas nuevas no
usan `onTopBarChange`: solo añaden su ruta al `when`.

El patrón antiguo (`onTopBarChange` + `LaunchedEffect` en cada pantalla) sigue
en algunas pantallas como legado. Puede dejar el título de la pantalla anterior
al restaurar estado o cambiar de pestaña; no lo copies en código nuevo. La
migración y el patrón correcto están en `top-bar.md`.

## Home

`HomeScreen.kt`

```text
Column(fillMaxSize + padding(AppSpacing.md), spacedBy = AppSpacing.sm)
├── StatisticsBanner(...)  // solo si hay tomas evaluadas en 7 días
└── LazyColumn(
│     contentPadding = PaddingValues(vertical = AppSpacing.sm),
│     verticalArrangement = spacedBy(AppSpacing.sm))
│   └── IntakeCard(...)
└── o BlankCard(home_empty_today) centrada (weight(1f))
```

- Título de la top bar: `home_title_today` con la fecha formateada en español;
  `showLogo = true`, resuelto en `MainScreen` a partir de la ruta.
- Usa `collectAsStateWithLifecycle()` y refresca el resumen con
  `LifecycleEventEffect(ON_RESUME)`.
- El banner abre `StatisticsScreens.Statistics`; se oculta si no hay ninguna
  toma evaluada en los últimos siete días.

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
- Hoy empuja el título con `onTopBarChange` (legado); lo correcto es resolverlo
  por ruta en `MainScreen`.

## Ficha de medicamento

`MedicationScreen.kt` con `MedicationMode.Create / Edit / View / Archived`.

- Raíz de cada modo:
  `Column(fillMaxSize + padding(AppSpacing.md) + verticalScroll)`.
- Orden:
  1. `AppPhotoPicker` con `bottom = AppSpacing.xl`; en edición permite cámara o
     galería y siempre recorta en 1:1.
  2. `AppFormSection(medication_data_section)` con los campos de datos.
  3. `AppFormSection(medication_schedule_section)` + `ScheduleEditor`.
  4. `AppFormActions` al final (`guardar`).
- En `View`: `AppScrollWithFooter` con `MedicationHeader`,
  `AppReadOnlySection` para «Tratamiento» y «Pauta», `ScheduleReadOnly`
  (resumen, chips de hora y fila «Próxima toma») y una sección «Seguimiento»
  con `AppNavigationRow` para abrir las estadísticas y el historial de ese
  medicamento. La foto de la cabecera abre `AppPhotoViewerDialog`. Las acciones
  (`editar` + archivar) cierran el contenido; si la ficha no llena la pantalla,
  se anclan abajo.
- Sin badge de «Crónico»: el estado se deduce de la fila «Fecha fin»
  (`medication_no_end_date`).
- En `Archived`: `MedicationArchivedContent` reutiliza `MedicationHeader`,
  `AppScrollWithFooter`, `AppNoticeBlock` con la fecha de archivo y las
  secciones «Tratamiento», «Pauta» (solo si se conserva) y «Seguimiento». El
  pie ofrece «Desarchivar» como acción principal y el borrado definitivo como
  secundaria roja; este último abre un `AlertDialog` de confirmación.
- La acción secundaria de `AppFormActions` sirve para archivar o eliminar; en
  `ProfileScreen` se marca con `secondaryContentColor = error`.
- `LaunchedEffect(currentMode, topBarTitle)` actualiza la top bar (patrón
  legado, pendiente de migrar); en modo Edit muestra `navigationAction` para
  volver a View y `BackHandler` hace lo mismo.

## Medicamentos archivados

`ArchivedMedicationsScreen.kt` con `ArchivedMedicationsViewModel`.

- Destino de detalle `medication/archived` dentro del subgrafo de medicamentos;
  no es una pestaña. Se abre desde la fila «Medicamentos archivados» de la
  ficha de perfil.
- Lista: `Column(fillMaxSize + padding(AppSpacing.md))` y `LazyColumn` con
  `MedicationCard` (`ver`) y `key = { it.id }`; estado vacío
  `BlankCard(medication_archived_empty)`.
- Cada item abre `medication/archived/{medicationId}`. La ficha usa
  `MedicationMode.Archived`; al desarchivar o eliminar definitivamente se
  vuelve a la lista.
- El perfil abre las estadísticas del medicamento archivado reutilizando
  `statistics/medication/{medicationId}`.

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
  1. `AppPhotoPicker` centrado; en edición permite cámara o galería y siempre
     recorta en 1:1.
  2. `AppFormSection(profile_personal_data)`.
  3. `AppFormSection(profile_medical_info)`.
  4. `AppFormSection(profile_admin_data)`.
  5. `AppFormActions` al final (`guardar`).
- En `View`: `ProfileViewContent` con `AppScrollWithFooter`, cabecera
  `ProfileHeader`, `AppReadOnlySection` + `AppDetailRow` por sección,
  `AppNoticeBlock` para las alergias, las filas de «Ver estadísticas» y
  «Medicamentos archivados» en «Seguimiento» (`AppNavigationRow` con
  `Icons.Rounded.Archive`) y la fila de «Ver historial de tomas» en
  «Historial». La foto de la cabecera abre `AppPhotoViewerDialog`. Las acciones
  (`editar` + eliminar) cierran el contenido.
- El diálogo de eliminación usa `AlertDialog` con textos de `strings.xml`.
- En `MainScreen` (modo View) hoy empuja el título con `onTopBarChange`
  (legado); la ruta debe resolverlo en `MainScreen` (`top-bar.md`).

## Estadísticas

`StatisticsScreen.kt` con `StatisticsViewModel`.

- Destino de detalle `statistics` dentro de `MainNavigationGraph`; no es una
  pestaña. La barra inferior y el FAB se ocultan solos, y la flecha atrás la
  resuelve `MainTopBar`.
- La ficha de medicamento abre `statistics/medication/{medicationId}`, que
  reutiliza la pantalla y el ViewModel filtrando las tomas de ese medicamento.
  En ese modo el resumen es `MedicationStatisticsCard`, se oculta «Por
  medicamento» y el estado vacío usa
  `BlankCard(statistics_empty_no_intakes_medication)`.
- `StatisticsContent` separa el contenido del ViewModel para previsualizarlo.
- Estructura general:
  `LazyColumn(padding(AppSpacing.md), spacedBy(AppSpacing.sm))` con:
  1. `StatisticsPeriodSelector` (7 / 30 / 90 días; 7 por defecto).
  2. `StatisticsSummaryCard` con porcentaje, tomadas, sin registrar y pendientes.
  3. `WeeklyStatisticsChart` con 7 días; `AdherenceLineChart` con 30 y 90 días
     (puntos semanales).
  4. Título «Por medicamento» + `MedicationStatisticsCard` por medicamento.
  5. Nota al pie.
- Estados vacíos: `BlankCard(statistics_empty_no_medications)` y
  `BlankCard(statistics_empty_no_intakes)`; en modo medicamento,
  `BlankCard(statistics_empty_no_intakes_medication)`. Si no hay tomas
  evaluadas en 30/90 días, el hueco del gráfico usa
  `BlankCard(statistics_chart_no_data)`.
- Los textos y la accesibilidad salen de `statistics_*`. El gráfico es Compose
  puro y cada barra tiene `contentDescription` con el día y los conteos.

## Historial

`HistoryScreen.kt` con `HistoryViewModel`.

- Destino de detalle `history?medicationId={medicationId}` dentro de
  `MainNavigationGraph`; no es una pestaña. La barra inferior y el FAB se
  ocultan, y la flecha atrás la resuelve `MainTopBar`.
- Se abre desde la sección «Historial» de la ficha del perfil con
  `medicationId = -1`, y desde la ficha de un medicamento con su id para
  preseleccionar ese medicamento.
- Estructura:
  `Column(fillMaxSize + padding(AppSpacing.md), spacedBy = AppSpacing.sm)` con:
  1. `AppSearchField` por nombre de medicamento.
  2. `HistoryFiltersButton`, que abre `HistoryFiltersSheet` y muestra el rango
     de fechas activo («Últimos 30 días» o las fechas personalizadas).
  3. Contador de resultados.
  4. `LazyColumn` con `HistoryDayHeader` + `HistoryIntakeItem` agrupados por día.
- `HistoryFiltersSheet` contiene estado, medicamento (incluidos archivados),
  interruptor «Incluir archivados» y rango personalizado con
  `AppDatePickerField`. Las fechas se aplican al cerrar con «Ver resultados»
  (`Primary` `Large`), con la X, con el gesto de arrastre o con atrás; «Limpiar
  filtros» (`Tonal` `Large`, sin icono) vuelve al rango por defecto de 30 días y
  cierra la hoja.
- Estados vacíos: `BlankCard` con `history_empty_no_medications`,
  `history_empty_no_intakes` o `history_empty_no_results`, y botón «Limpiar
  filtros» (`Ghost` `Large`) cuando hay filtros aplicados.
- Los textos salen de `history_*` y `profile_history_*`. Los estados de las
  tomas se recalculan con `ON_RESUME` y un ticker de 60 segundos.

## Ajustes

`SettingsScreen.kt`

- `Scaffold` con `MainTopBar` y `SnackbarHost`.
- Column con scroll:
  `padding(horizontal = AppSpacing.md, vertical = AppSpacing.md)` y
  `spacedBy(AppSpacing.sm)`.
- Bloques:
  1. Apariencia: `SettingsCard` con título «Tema» + `SettingsRadioItem` para
     Seguir al sistema / Claro / Oscuro.
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
2. Añade la ruta al resolver de `TopBarState` de `MainScreen`; no uses
   `onTopBarChange` ni un `LaunchedEffect` para el título.
3. Si la pantalla vive fuera de `MainScreen` y necesita su propio `Scaffold`
   (por ejemplo, un FAB local), reutiliza `MainTopBar` y pásale
   `topLevelRoutes`.
4. Usa `AppSpacing` para el margen y las separaciones.
5. Usa `BlankCard` para el estado vacío, centrada.
6. Si hay formulario, divide con `AppFormSection`, cierra con `AppFormActions`
   y no mezcles lectura y edición.
7. Añade los textos a `strings.xml` y el `@Preview` del componente nuevo.
8. En código nuevo, consume estado con `collectAsStateWithLifecycle()`.
9. Comprueba claro/oscuro y texto grande antes de terminar.
