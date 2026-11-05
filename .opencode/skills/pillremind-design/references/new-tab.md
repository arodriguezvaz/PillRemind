# Añadir una pestaña nueva

Receta para una pestaña de primer nivel en la barra inferior de PillRemind. Si
la pantalla es un detalle (ficha, edición, creación), no la añadas a la barra:
créala como destino aparte y vuelve a la pestaña con `popBackStack()`.

## Decisión previa

| Caso | Dónde va |
|---|---|
| Sección principal nueva | `BottomBarScreens` + `MainBottomBar` + `MainNavigationGraph` |
| Detalle de una pestaña | `composable` con argumentos en `MainNavGraph` |
| Flujo con varias pantallas | Subgrafo tipo `medicationNavGraph` |
| Pantalla fuera de la barra inferior | `profileSelectionGraph` o grafo raíz |

La barra inferior actual tiene tres destinos: Home, Medicamentos y Perfil. No
añadas más de cinco; si el flujo es secundario, usa un acceso desde una pantalla
existente.

## Archivos que se tocan

| Archivo | Cambio |
|---|---|
| `app/src/main/res/values/strings.xml` | `nav_<feature>` y textos de la pantalla |
| `navigation/BottomBarScreens.kt` | Nuevo `object` con ruta, título e icono |
| `ui/screens/main/MainBottomBar.kt` | Añadir la pestaña a `screens` |
| `navigation/MainNavGraph.kt` | `composable` de la pestaña |
| `ui/screens/main/MainScreen.kt` | Ruta en `topLevelRoutes` y en el resolver de `TopBarState` |
| `ui/screens/<feature>/<Feature>Screen.kt` | Pantalla nueva |
| `ui/screens/<feature>/<Feature>ViewModel.kt` | Estado y acciones |
| `navigation/<Feature>NavGraph.kt` | Solo si hay detalles o creación |

## Top bar: la ruta es la fuente de verdad

La top bar de `MainScreen` se resuelve con la ruta actual, no con un efecto de
la pantalla. Una pestaña nueva:

- **No** recibe `onTopBarChange`.
- **No** tiene un `LaunchedEffect` para el título.
- Añade su ruta al `when` de `TopBarState` en `MainScreen`.

Si `MainScreen` todavía usa el patrón legado `onTopBarChange` +
`LaunchedEffect`, aplica primero `top-bar.md`. Ese patrón puede dejar el título
de la pantalla anterior al restaurar estado o cambiar de pestaña.

## 1. Strings

Añade los textos en `strings.xml`, en la sección de navegación y en la sección
de la feature. No hardcodees nada en Compose.

```xml
<!-- Navegación -->
<string name="nav_history">Historial</string>

<!-- Historial -->
<string name="history_title">Historial</string>
<string name="history_empty">Todavía no hay tomas registradas</string>
<string name="history_item_dose">%1$s %2$s · %3$s</string>
```

Prefijos habituales: `nav_`, `common_`, `profile_`, `medication_`, `schedule_`,
`settings_`, `backup_`, `notification_`, `onboarding_`. Para una feature nueva,
usa el nombre de la feature como prefijo (`history_`).

## 2. `BottomBarScreens`

En `navigation/BottomBarScreens.kt`:

```kotlin
object History : BottomBarScreens(
    route = "history",
    titleRes = R.string.nav_history,
    icon = Icons.Rounded.History,
)
```

- La ruta es corta, en minúsculas y sin parámetros.
- El icono es `Icons.Rounded.*`, como en Home, Medications y Profile.
- El título sale siempre de `R.string.nav_*`.

## 3. `MainBottomBar`

En `ui/screens/main/MainBottomBar.kt`, añade la pestaña a la lista, en el orden
visual deseado:

```kotlin
val screens = listOf(
    BottomBarScreens.Home,
    BottomBarScreens.Medications,
    BottomBarScreens.History,
    BottomBarScreens.Profile,
)
```

No dupliques la lógica de selección ni los colores: ya está resuelta en
`AddItem`.

## 4. `MainNavigationGraph`

En `navigation/MainNavGraph.kt`, añade el destino dentro del `NavHost`:

```kotlin
composable(BottomBarScreens.History.route) {
    HistoryScreen(
        onHistoryClick = { id ->
            navController.navigate(HistoryScreens.Detail.createRoute(id))
        },
    )
}
```

- La pantalla solo recibe callbacks de navegación, no `onTopBarChange`.
- Para un detalle:

```kotlin
composable(
    route = HistoryScreens.Detail.route,
    arguments = listOf(navArgument("itemId") { type = NavType.IntType }),
) {
    HistoryDetailScreen()
}
```

Y define las rutas en un `sealed class`, como `MedicationScreens`:

```kotlin
sealed class HistoryScreens(val route: String) {
    object Detail : HistoryScreens("history/detail/{itemId}") {
        fun createRoute(itemId: Int) = "history/detail/$itemId"
    }
}
```

Al abrir un detalle, la barra inferior se oculta porque la ruta actual ya no
coincide con `BottomBarScreens`; es el comportamiento actual, igual que en la
ficha de medicamento. El detalle muestra flecha atrás y vuelve a la pestaña con
`popBackStack()`.

## 5. `MainScreen`

En `ui/screens/main/MainScreen.kt`:

```kotlin
val topLevelRoutes = remember {
    setOf(
        BottomBarScreens.Home.route,
        BottomBarScreens.Medications.route,
        BottomBarScreens.History.route,
        BottomBarScreens.Profile.route,
    )
}
```

Esto evita que la pestaña muestre flecha atrás. Las rutas de detalle **no** van
en `topLevelRoutes`: así muestran flecha y vuelven a la pestaña.

Añade la ruta al resolver de `TopBarState`:

```kotlin
val defaultTopBarState = when (currentRoute) {
    BottomBarScreens.Home.route -> TopBarState(
        title = homeTopBarTitle(),
        showLogo = true,
    )
    BottomBarScreens.Medications.route -> TopBarState(
        title = stringResource(R.string.nav_medications),
    )
    BottomBarScreens.History.route -> TopBarState(
        title = stringResource(R.string.nav_history),
    )
    BottomBarScreens.Profile.route -> TopBarState(
        title = stringResource(R.string.nav_profile),
    )
    else -> TopBarState(title = stringResource(R.string.nav_home))
}
```

`currentRoute` sale de `navController.currentBackStackEntryAsState()`. Si
`MainScreen` aún no lo tiene, adóptalo siguiendo `top-bar.md`.

### FAB: cuidado

`MainScreen` tiene un FAB global cuyo `onClick` está cableado a
`MedicationScreens.MedicationCreateScreen`. Por eso `showFab` solo incluye Home
y Medicamentos:

```kotlin
val showFab = currentRoute == BottomBarScreens.Home.route ||
    currentRoute == BottomBarScreens.Medications.route
```

No añadas la pestaña nueva a `showFab` si su acción no es crear un medicamento:
abrirías la pantalla equivocada.

Si la pestaña necesita crear algo:

- **Opción recomendada:** generaliza el FAB de `MainScreen` para mapear ruta →
  acción (`showFab` + `onFabClick`), en lugar de dejar la navegación cableada a
  medicamentos.
- **Alternativa:** coloca la acción de creación como un `AppButton` dentro de la
  pantalla (por ejemplo, en la cabecera o al final de la lista).

No añadas un `Scaffold` propio a una pestaña que ya vive en `MainScreen`:
tendrías dos top bars y doble padding. El patrón `ProfilesScreen`
(`Scaffold` + `AppFab`) es para pantallas que **no** están dentro de
`MainScreen`, como la selección de perfiles o ajustes.

### Subgrafos

`MainBottomBar` solo se muestra cuando la ruta actual coincide exactamente con
una ruta de `BottomBarScreens`. Si conviertes la pestaña en un subgrafo, la
barra puede desaparecer en la pantalla inicial del subgrafo y, como la ruta
actual será la del hijo, `topLevelRoutes` (que contiene la ruta del grafo) no la
reconocerá y puede aparecer la flecha atrás. Para una pestaña simple, usa un
`composable` raíz; reserva `navigation(...)` para flujos con varias pantallas y
verifica la barra inferior y la flecha atrás en cada destino.

## 6. Esqueleto de la pantalla

Crea `ui/screens/history/HistoryScreen.kt`. Si la pantalla vive dentro del
`Scaffold` de `MainScreen`, **no añadas otro `Scaffold`**. Los nombres
`History*` del ejemplo son placeholders: sustitúyelos por los de tu feature.

```kotlin
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    onHistoryClick: (Int) -> Unit = {},
    historyViewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by historyViewModel.uiState.collectAsStateWithLifecycle()

    HistoryContent(
        uiState = uiState,
        onHistoryClick = onHistoryClick,
        modifier = modifier,
    )
}

@Composable
private fun HistoryContent(
    uiState: HistoryUiState,
    onHistoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppSpacing.md),
    ) {
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BlankCard(stringResource(R.string.history_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    HistoryCard(
                        item = item,
                        onClick = { onHistoryClick(item.id) },
                    )
                }
            }
        }
    }
}
```

Reglas del esqueleto:

- `HistoryScreen` obtiene el ViewModel con `hiltViewModel()`.
- `HistoryContent` es stateless y se puede previsualizar.
- El título **no** se empuja desde aquí: lo resuelve `MainScreen` por ruta.
- El margen de pantalla es `AppSpacing.md`.
- La lista usa `spacedBy(AppSpacing.sm)` y `key` estable.
- El estado vacío es `BlankCard` centrada.
- Si el contenido es un formulario, usa
  `verticalScroll + imePadding + padding(AppSpacing.md)`, `AppFormSection` y
  `AppFormActions`.

## 7. Estado

Para esta app, el contrato mínimo es:

- `@HiltViewModel` con dependencias inyectadas.
- Estado expuesto como `StateFlow`.
- La pantalla lo consume con `collectAsStateWithLifecycle()`.
- Errores como `@StringRes Int`, no textos literales.
- Sin navegación en el ViewModel: la pantalla recibe callbacks.

No repitas aquí patrones genéricos de Compose o Hilt: para eso están las
skills `android-*`.

## 8. Previews

```kotlin
@Preview(showBackground = true, name = "Historial claro")
@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Historial oscuro",
)
@Preview(showBackground = true, fontScale = 1.5f, name = "Historial texto grande")
@Composable
private fun HistoryContentPreview() {
    PillRemindTheme {
        HistoryContent(
            uiState = HistoryUiState(),
            onHistoryClick = {},
        )
    }
}
```

- Previsualiza el contenido stateless, no la pantalla con Hilt.
- Incluye claro, oscuro y `fontScale = 1.5f`.
- Añade estados con datos y vacío si el layout cambia.

## 9. Verificación

```bash
cd PillRemind
JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug
```

Checklist específica de pestaña:

- [ ] `nav_<feature>` existe en `strings.xml`.
- [ ] `BottomBarScreens` tiene ruta, `titleRes` e icono.
- [ ] `MainBottomBar` incluye la pestaña.
- [ ] `MainNavigationGraph` tiene su `composable` y no pasa `onTopBarChange`.
- [ ] `MainScreen.topLevelRoutes` incluye la ruta raíz.
- [ ] `MainScreen` resuelve el título de la ruta en su `when` de `TopBarState`.
- [ ] La pantalla no usa `onTopBarChange` ni un `LaunchedEffect` para el título.
- [ ] No se añadió a `showFab` sin cambiar la acción del FAB.
- [ ] La pestaña no añade un `Scaffold` propio si ya vive en `MainScreen`.
- [ ] La barra inferior sigue visible en la pestaña.
- [ ] Claro/oscuro y texto grande comprobados.
- [ ] `assembleDebug` compila.

## Errores comunes

| Error | Síntoma |
|---|---|
| Olvidar `topLevelRoutes` | Aparece flecha atrás en una pestaña principal. |
| Usar `onTopBarChange` + `LaunchedEffect` | El título puede quedarse en la pantalla anterior al restaurar estado o cambiar de pestaña. |
| Olvidar la ruta en el `when` de `TopBarState` | La pestaña muestra el título por defecto. |
| Añadir la ruta a `showFab` | El FAB abre la creación de medicamento. |
| Añadir un `Scaffold` dentro de `MainScreen` | Doble barra, doble padding o contenido tapado. |
| Usar `collectAsState()` en código nuevo | El estado no respeta el ciclo de vida. |
| No poner `key` en `LazyColumn` | Saltos y recomposiciones al cambiar la lista. |
| Hardcodear textos o colores | La UI no se adapta al tema ni a traducciones. |
| No comprobar el modo oscuro | Contrastes rotos en `surface`, `onSurface` o `error`. |
