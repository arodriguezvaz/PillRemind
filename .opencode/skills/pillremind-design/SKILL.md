---
name: PillRemind Design
description: Úsala al crear o modificar pantallas, pestañas, componentes o estilos Compose en PillRemind: colores, tipografía, formas, espaciado, elevación, modo oscuro, strings, accesibilidad y receta de pestaña nueva.
---

# Diseño de PillRemind

Sistema visual y convenciones de interfaz de la app. Léela antes de tocar
`PillRemind/app/src/main/java/com/andres/pillremind/ui/` o la navegación.

Esta skill no repite las skills `android-*`: recoge solo las decisiones de UI
propias de PillRemind (tokens, componentes, pantallas, top bar y navegación
visual).

## Fuentes de verdad

| Tema | Archivo |
|---|---|
| Color | `ui/theme/Color.kt` |
| Tipografía | `ui/theme/Type.kt` y `FontFamily.kt` |
| Formas | `ui/theme/Shapes.kt` (`AppShapes`, `AppImageShape`) |
| Espaciado | `ui/theme/Spacing.kt` (`AppSpacing`) |
| Elevación | `ui/theme/Elevation.kt` (`AppElevation`) |
| Tema | `ui/theme/Theme.kt` |
| Componentes | `ui/components/` y `ui/components/settings/` |
| Pantallas | `ui/screens/` |
| Navegación | `navigation/` |
| Top bar | `ui/screens/main/` (`MainScreen`, `TopBarState`, `MainTopBar`) |
| Textos | `app/src/main/res/values/strings.xml` |

Si el código y esta skill discrepan, manda el código. La excepción es la top
bar: el patrón `onTopBarChange` + `LaunchedEffect` que aún existe en pantallas
antiguas es legado; para pestañas nuevas manda el patrón derivado de la ruta
(`references/top-bar.md`). Si el cambio es intencionado, actualiza la skill en
el mismo cambio.

## Principios

1. **Pensada para personas mayores.** El texto de lectura usa `bodyLarge`
   (16sp); `bodyMedium` (14sp) se reserva para descripciones y `bodySmall`
   (12sp) para metadatos. Los valores de `AppDetailRow` suben a 18sp/26sp.
   No reduzcas texto para que quepa: reordena el layout.
2. **Una sola elevación.** `AppElevation` (4dp) en cards, campos, chips, filas
   de horas, botones y FAB. No añadas 8dp, 12dp ni sombras propias.
3. **Superficies limpias.** Fondo `background`, cards y campos `surface`, texto
   principal `onSurface`, secundario `onSurfaceVariant`.
4. **Claro y oscuro.** Comprueba ambos; no uses blanco/negro fijos. El tema
   oscuro usa fondo negro y un primario azul cielo, y reserva el naranja, el
   verde y el rojo para los estados. El modo dinámico está desactivado en
   `PillRemindTheme`.
5. **Color solo desde el tema.** `Color(0x...)` vive únicamente en `Color.kt`.
   En pantallas y componentes usa `MaterialTheme.colorScheme`.
6. **Reutiliza antes de crear.** Si un componente de `ui/components` cubre el
   caso, úsalo. Si creas uno nuevo, va en `ui/components`, con KDoc y `@Preview`.
7. **Nada de textos hardcodeados.** Todo texto visible y todo
   `contentDescription` salen de `strings.xml`. Los ViewModels exponen
   `@StringRes Int`, no mensajes literales.

## Tokens

Resumen operativo; los valores completos están en `references/tokens.md`.

### Color

| Rol | Uso |
|---|---|
| `primary` | Marca, títulos de sección, acción principal, icono activo. Negro en claro, blanco en oscuro. |
| `onPrimary` | Texto e iconos sobre `primary`. |
| `primaryContainer` / `onPrimaryContainer` | Fondos suaves: chip seleccionado, avatar por defecto. |
| `secondaryContainer` / `onSecondaryContainer` | Botón de una toma ya realizada. |
| `error` / `onError` | Acciones destructivas y toma retrasada. |
| `surface` | Fondo de cards, campos, chips no seleccionados y filas. |
| `surfaceVariant` | Campos deshabilitados y fondo del selector de foto. |
| `onSurfaceVariant` | Texto secundario, metadatos y descripciones. |
| `outlineVariant` | Divisores y bordes finos. |
| `background` | Fondo general y barra superior. |

No uses `secondary`/`tertiary` para decorar: cada rol tiene un significado.
`tertiary` es el verde de confirmación y hoy apenas se usa.

### Tipografía (Lexend)

| Rol | Uso |
|---|---|
| `titleLarge` | Títulos de sección (`AppFormSection`, `AppReadOnlySection`) y título de la top bar. |
| `titleMedium` | Título de card (`MedicationCard`, `IntakeCard`, `PerfilCard`, `SettingsCard`). |
| `bodyLarge` | Texto de lectura, etiquetas de ajustes, horas. |
| `bodyMedium` | Descripciones, enlaces de ajustes y estado vacío. |
| `bodySmall` | Metadatos (dosis y hora) y avisos secundarios. |
| `labelLarge` | Etiqueta de campo y botón mediano. |
| `labelMedium` | Chips y botón pequeño. |
| `headline*` / `display*` | Reservados; no hay uso actual. |

### Formas, espaciado y elevación

| Token | Valor | Uso |
|---|---|---|
| `shapes.medium` | 12dp | Campos, desplegables, fecha, botones, chips y filas de horas. |
| `shapes.large` | 20dp | Cards de contenido, info, ajustes y estados vacíos. |
| `shapes.extraLarge` | 28dp | Hojas y contenedores grandes. |
| `AppImageShape` | 14dp | Recorte de fotos, avatares y miniaturas. |
| `AppSpacing.md` | 16dp | Margen de pantalla y relleno de cards. |
| `AppSpacing.sm` | 12dp | Entre cards, campos y relleno vertical de cards de lista. |
| `AppSpacing.xs` | 8dp | Etiqueta → campo, chips y controles relacionados. |
| `AppElevation` | 4dp | Única elevación de la app. |

No escribas `RoundedCornerShape(...)` en pantallas o componentes: usa un token.
Excepciones que se mantienen como literales: tamaños de iconos/imágenes/botones,
paddings internos de `AppButton`, 10dp de un bloque de `SettingsScreen` y
bordes/divisores de 1/2dp.

## Componentes

Catálogo detallado en `references/components.md`.

- **Botones:** `AppButton` (`Primary`, `Secondary`, `Tonal`, `Ghost`, `Danger`;
  `Small` 110dp, `Medium` 160dp, `Large` ancho completo) y `AppFab`.
- **Campos:** `AppTextField` + `AppFormField`; `AppSearchField` para búsqueda;
  `AppDropdownField`, `AppDatePickerField`, `AppPhotoPicker`.
- **Formularios:** `AppFormSection` y `AppFormActions`.
- **Solo lectura:** `AppReadOnlySection`, `AppDetailRow` (con variante
  `stacked`), `AppNoticeBlock` y `AppScrollWithFooter`.
- **Cards:** `BlankCard`, `MedicationCard`, `IntakeCard`, `PerfilCard`.
- **Ajustes:** `SettingsCard`, `SettingsRadioItem`, `SettingsToggleItem`,
  `SettingsLinkItem`.
- **Horarios:** `WeekDayChipGroup`, `TimeListEditor`, `TimeItem`, `AddTimeItem`.

## Patrones de pantalla

- **Raíz de pestaña:** la pestaña vive dentro del `Scaffold` de `MainScreen`;
  no añadas otro `Scaffold`. Usa
  `Column(Modifier.fillMaxSize().padding(AppSpacing.md))`.
- **Lista:** `LazyColumn` con
  `contentPadding = PaddingValues(vertical = AppSpacing.sm)`,
  `verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)` y `key` estable.
- **Vacío:** `BlankCard` centrada en el espacio disponible.
- **Formulario:** `Column` con `verticalScroll` + `imePadding` + `padding(md)`;
  secciones con `AppFormSection`; cierre con `AppFormActions`.
- **Card:** `Card(shape = MaterialTheme.shapes.large,
  colors = surface, elevation = AppElevation)`.
- **Navegación:** `MainScreen` deriva `TopBarState` de la ruta actual
  (`currentBackStackEntryAsState()`) y de un `when` de rutas. La pantalla no
  empuja su título. El patrón `onTopBarChange` + `LaunchedEffect` es legado y
  puede dejar la top bar desincronizada; no lo uses en pestañas nuevas. Detalle
  en `references/top-bar.md`.
- **Ajustes:** una `SettingsCard` por bloque; usa `SettingsRadioItem` para
  opciones exclusivas, `SettingsToggleItem` para binarias y `SettingsLinkItem`
  para acciones.
- **Detalle:** si la pantalla vive fuera de `MainScreen` y necesita su propio
  `Scaffold`, reutiliza `MainTopBar` y pásale `topLevelRoutes` (patrón
  `ProfilesScreen`). Dentro de `MainScreen` no añadas otro `Scaffold`.

## Pestaña nueva

No improvises el cableado. Sigue `references/new-tab.md`, que cubre
`BottomBarScreens`, `MainBottomBar`, `MainNavigationGraph`, `MainScreen`,
strings, FAB, esqueleto de pantalla y checklist.

Reglas rápidas:

- Una pestaña de primer nivel se añade a `BottomBarScreens`, `MainBottomBar`,
  `MainNavigationGraph`, `MainScreen.topLevelRoutes` y al resolver de
  `TopBarState` de `MainScreen`.
- No añadas `onTopBarChange` ni un `LaunchedEffect` para el título: la ruta
  nueva va al `when` de `MainScreen` (`references/top-bar.md`).
- El FAB de `MainScreen` está cableado a la creación de medicamentos. No añadas
  una ruta nueva a `showFab` si su acción no es esa. Si la pestaña necesita
  crear, generaliza el FAB de `MainScreen` con una acción por ruta o abre la
  creación desde un botón de la propia pantalla; no añadas un `Scaffold` propio
  dentro de `MainScreen` (el patrón `ProfilesScreen` es para pantallas fuera de
  `MainScreen`).
- No añadas una pestaña de detalle a la barra inferior: usa un destino anidado.
- La barra inferior solo se muestra cuando la ruta actual coincide exactamente
  con una ruta de `BottomBarScreens`; si usas un subgrafo, comprueba también que
  no aparezca la flecha atrás en su pantalla inicial.

## Strings y accesibilidad

- Ningún texto visible hardcodeado: `stringResource` o `context.getString`.
- Prefijos en `strings.xml`: `nav_`, `common_`, `profile_`, `medication_`,
  `schedule_`, `settings_`, `backup_`, `notification_`, `onboarding_`.
- `contentDescription` siempre desde recursos; iconos decorativos con `null`.
- Área táctil mínima 48dp (`IconButton`); filas pulsables con `onClickLabel`.
- En cards, `maxLines = 1` + `TextOverflow.Ellipsis`; no recortes etiquetas.
- Deshabilitado: alpha 0.5 o colores `disabled` del tema, no grises inventados.
- Textos largos: `maxLines` y elipsis en títulos; envuelve descripciones.

## Previews y verificación

- Todo componente nuevo lleva `@Preview(showBackground = true)`.
- Una pantalla con ViewModel se previsualiza separando `XScreen` (Hilt) de
  `XContent` (sin ViewModel), o previsualizando el contenido con estado falso.
- Comprueba claro y oscuro, y `fontScale = 1.5f`.
- Antes de terminar:

```bash
cd PillRemind
JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug
```

## Checklist antes de terminar

- [ ] Leí `ui/theme` y reutilizo tokens y componentes.
- [ ] No hay `Color(0x...)` ni formas literales fuera de las excepciones.
- [ ] `AppSpacing` en paddings, spacers y separaciones.
- [ ] Textos en `strings.xml` y `contentDescription` correctos.
- [ ] Comprobado en claro y oscuro, y con texto grande.
- [ ] La pestaña nueva está en el resolver de `TopBarState` de `MainScreen`;
      no usa `onTopBarChange` ni un `LaunchedEffect` para el título.
- [ ] Si es pestaña, seguí `references/new-tab.md`.
- [ ] Componente nuevo: KDoc + `@Preview`.
- [ ] `assembleDebug` compila.

## Referencias

- `references/tokens.md`: valores completos y uso de color, tipografía, formas,
  espaciado y elevación.
- `references/components.md`: ficha y uso de cada componente.
- `references/screens.md`: estructura de las pantallas actuales.
- `references/top-bar.md`: top bar derivada de la ruta y migración del patrón
  legado.
- `references/new-tab.md`: receta completa para añadir una pestaña.
