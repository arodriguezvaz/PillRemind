# Top bar de PillRemind

## Regla

La top bar de `MainScreen` se deriva de la ruta actual. La pantalla no empuja su
título.

## Patrón actual (legado)

`MainScreen` guarda un `TopBarState` y cada pantalla lo actualiza así:

```kotlin
LaunchedEffect(title) {
    onTopBarChange(TopBarState(title = title))
}
```

Funciona en el caso fácil, pero el título no depende de la navegación: depende
de que el efecto de la pantalla se ejecute. Con `saveState/restoreState` y
cambios de pestaña, la composición puede restaurarse sin que el efecto vuelva a
ejecutarse y la top bar conserva el título anterior. Añadir `LaunchedEffect` no
lo arregla: el efecto forma parte del problema. No bases la corrección en que
hoy las transiciones estén desactivadas; el problema es el estado empujado, no
la duración de la transición.

## Patrón recomendado

`MainScreen` ya observa `currentBackStackEntryAsState()`. Deriva el estado de la
ruta:

```kotlin
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route

val defaultTopBarState = when (currentRoute) {
    BottomBarScreens.Home.route -> TopBarState(
        title = homeTopBarTitle(),
        showLogo = true,
    )
    BottomBarScreens.Medications.route -> TopBarState(
        title = stringResource(R.string.nav_medications),
    )
    BottomBarScreens.Profile.route -> TopBarState(
        title = stringResource(R.string.nav_profile),
    )
    BottomBarScreens.History.route -> TopBarState(
        title = stringResource(R.string.nav_history),
    )
    else -> TopBarState(title = stringResource(R.string.nav_home))
}
```

`homeTopBarTitle()` es el título con la fecha que hoy calcula `HomeScreen`.
Extráelo a una función de `ui/screens/home` o `ui/screens/main` para que
`MainScreen` y la pantalla compartan el mismo texto:

```kotlin
@Composable
fun homeTopBarTitle(): String {
    val today = LocalDate.now()
    val pattern = stringResource(R.string.home_date_pattern)
    val formatted = remember(today, pattern) {
        today.format(
            DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es"))
        )
    }
    return stringResource(R.string.home_title_today, formatted)
}
```

En `MainScreen`, sustituye el `var topBarState by remember { ... }` por
`defaultTopBarState` (o por `override ?: defaultTopBarState` si conservas el
patrón legado). El `Scaffold` sigue recibiendo `topBarState`.

Si una pantalla antigua sigue necesitando un título dinámico por modo
(medicamento/perfil en creación, edición o vista), mantén `onTopBarChange` como
override solo para esa ruta y migra el modo a la ruta cuando puedas. No lo uses
en pestañas nuevas. Las rutas de detalle que no estén en el `when` caen en el
`else`; si aún se titulan con `onTopBarChange`, conserva ese override hasta
migrarlas.

## Cómo queda una pestaña nueva

1. Añade su ruta al `when` de `MainScreen`.
2. La pantalla no recibe `onTopBarChange`.
3. La pantalla no tiene `LaunchedEffect` para el título.
4. `topLevelRoutes` sigue conteniendo solo las rutas raíz de las pestañas.

## Checklist

- [ ] `MainScreen` deriva `TopBarState` de `currentBackStackEntryAsState()`.
- [ ] La ruta nueva está en el `when`.
- [ ] La pantalla nueva no usa `onTopBarChange`.
- [ ] Probado: cambiar de pestaña, volver atrás y restaurar estado.
- [ ] El título correcto aparece al entrar y al volver.
