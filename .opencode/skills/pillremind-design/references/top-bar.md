# Top bar de PillRemind

## Regla

`MainScreen` es el único dueño de `TopBarState`. Los títulos estáticos se
derivan de la ruta actual (`currentBackStackEntryAsState()` + `when`); los
títulos dinámicos (perfil o medicamento en creación/edición) se publican desde
la pantalla y se guardan por ruta.

`MainTopBar` es presentacional: recibe `state`, `canGoBack` y `onBack`. No
recibe `NavHostController` ni `topLevelRoutes`.

## Estado derivado de la ruta

```kotlin
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route

val topLevelRoutes = remember {
    setOf(
        BottomBarScreens.Home.route,
        BottomBarScreens.Medications.route,
        BottomBarScreens.Profile.route,
    )
}
val canGoBack = currentRoute !in topLevelRoutes &&
    navController.previousBackStackEntry != null

val defaultTopBarState = when (currentRoute) {
    BottomBarScreens.Home.route -> TopBarState(
        title = "",          // la fecha vive en el contenido de Home
        showLogo = true,
    )
    BottomBarScreens.Medications.route -> TopBarState(
        title = stringResource(R.string.nav_medications),
    )
    BottomBarScreens.Profile.route -> TopBarState(
        title = stringResource(R.string.profile_view_title),
    )
    MedicationScreens.MedicationSeeScreen.route -> TopBarState(
        title = stringResource(R.string.medication_view_title),
    )
    MedicationScreens.MedicationCreateScreen.route -> TopBarState(
        title = stringResource(R.string.medication_add_title),
    )
    else -> TopBarState(title = stringResource(R.string.nav_home))
}

val topBarStates = remember { mutableStateMapOf<String, TopBarState>() }
val topBarState = resolveTopBarState(
    currentRoute = currentRoute,
    statesByRoute = topBarStates,
    fallback = defaultTopBarState,
)
```

Los overrides solo se usan en pantallas con estado dinámico:

```kotlin
composable(BottomBarScreens.Profile.route) {
    ProfileScreen(
        onTopBarChange = topBarChangeFor(BottomBarScreens.Profile.route, onTopBarChange),
    )
}
```

`ProfileScreen` y `MedicationScreen` publican su modo con `LaunchedEffect`; es
una transición hasta que el modo viva en la ruta (`profile/edit`,
`medication/edit/{id}`). No lo copies en pestañas nuevas.

El patrón antiguo (un único `TopBarState` global + `LaunchedEffect` en cada
pantalla) es el que provocaba el título pegado: con `saveState/restoreState` la
composición puede reutilizarse y el efecto no se ejecuta. No lo reintroduzcas;
tampoco bases la corrección en que hoy no haya transición.

## Contrato de MainTopBar

```kotlin
MainTopBar(
    state = topBarState,
    canGoBack = canGoBack,
    onBack = { navController.popBackStack() },
)
```

- Es presentacional: no conoce rutas ni el `NavHostController`.
- `state.navigationAction` tiene prioridad sobre el botón atrás.
- `onBack` usa `popBackStack()`, como recomienda la guía de Compose para la
  top bar. Dentro del subgrafo de medicamentos, `navigateUp()` puede subir al
  inicio del subgrafo en vez de volver a la lista.
- El título va en `maxLines = 1` con `TextOverflow.Ellipsis`.
- El logo y el título comparten una sola línea (`Row`); el logo es decorativo
  (`clearAndSetSemantics`).

## Home

La barra muestra el logo; la fecha vive en el contenido (`homeDateTitle()` +
`heading()`). No apiles logo y título en una `Column` dentro del small app bar
(64dp): dos líneas de `titleLarge` no caben con `fontScale = 1.5` o `2.0`.

Tampoco uses `MediumTopAppBar` como parche de accesibilidad: colapsa a 64dp,
usa una tipografía mayor y las variantes baseline medium/large ya no son las
recomendadas por Material 3.

## Pestaña nueva

1. Añade su ruta al `when` de `MainScreen`.
2. La pantalla no recibe `onTopBarChange` ni tiene un `LaunchedEffect` para el
   título.
3. `topLevelRoutes` sigue conteniendo solo las rutas raíz de las pestañas.

## Checklist

- [ ] `MainScreen` deriva `TopBarState` de `currentBackStackEntryAsState()`.
- [ ] La ruta nueva está en el `when`.
- [ ] La pantalla nueva no usa `onTopBarChange`.
- [ ] `MainTopBar` recibe `state`, `canGoBack` y `onBack`; no `NavHostController`.
- [ ] Título con `maxLines = 1` + elipsis; logo decorativo.
- [ ] Probado: cambiar de pestaña, volver atrás, `fontScale = 1.5` y `2.0`,
      RTL y restaurar estado.
