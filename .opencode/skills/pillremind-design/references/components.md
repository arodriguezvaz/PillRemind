# Catálogo de componentes

Todos viven en `PillRemind/app/src/main/java/com/andres/pillremind/ui/components/`
(salvo los de ajustes, en `components/settings/`, los de horarios, en
`components/timeeditor/`, y los de estadísticas, en `components/statistics/`).
Reutiliza estos componentes antes de crear uno nuevo. Los textos que reciben
son siempre cadenas ya resueltas con `stringResource`.

## Botones

### `AppButton` (`AppButton.kt`)

```kotlin
AppButton(
    text, onClick, modifier, variant, size, enabled, isLoading, leadingIcon
)
```

- Variantes: `Primary` (`primary`), `Secondary` (`secondaryContainer`),
  `Tonal` (`primaryContainer`), `Ghost` (transparente + `primary`),
  `Danger` (`error`).
- Tamaños: `Small` 110dp, `Medium` 160dp, `Large` ancho completo.
- `isLoading` deshabilita el botón y muestra un spinner de 16dp.
- `leadingIcon` es decorativo (`contentDescription = null`).
- Elevación `AppElevation`; deshabilitado sin elevación.

### `AppFab` (`AppFab.kt`)

```kotlin
AppFab(onClick, modifier, content)
```

- `FloatingActionButton` con `primary`/`onPrimary` y `AppElevation`.
- `content` suele ser un icono `Icons.Rounded.Add` con `contentDescription`
  desde recursos.
- El FAB de `MainScreen` solo aparece en Home y Medicamentos, y siempre navega
  a la creación de medicamento. Para una pantalla fuera de `MainScreen` con
  acción propia, usa un `Scaffold` de pantalla + `AppFab` (patrón
  `ProfilesScreen`); dentro de una pestaña de `MainScreen`, no añadas otro
  `Scaffold`.

## Campos y formularios

### `AppTextField` (`AppTextField.kt`)

```kotlin
AppTextField(
    value, onValueChange, modifier, placeholder, enabled, keyboardType,
    singleLine, minLines, maxLines, leadingIcon, trailingIcon, imeAction
)
```

- `Surface` con `shapes.medium`, `AppElevation` y color `surface`.
- `TextField` sin indicadores; contenedor `surface`; placeholder con elipsis.
- `singleLine = true` por defecto.

### `AppSearchField` (`AppSearchField.kt`)

```kotlin
AppSearchField(value, onValueChange, placeholder, modifier)
```

- `AppTextField` con lupa y botón de limpiar (`search_clear`) cuando hay texto.
- `imeAction = ImeAction.Search`.

### `AppFormField` (`AppFormField.kt`)

```kotlin
AppFormField(label, modifier, labelColor, labelStyle, content)
```

- Etiqueta `labelLarge` en `onSurface` y 8dp (`AppSpacing.xs`) hasta el campo.
- `labelColor` y `labelStyle` son opcionales; no los cambies sin motivo.

### `AppFormSection` (`AppFormSection.kt`)

```kotlin
AppFormSection(title, modifier, contentSpacing = AppSpacing.sm, content)
```

- Título `titleLarge` en `primary`, 12dp (`AppSpacing.sm`) y contenido en columna.
- `contentSpacing = 0.dp` cuando los campos ya llevan su propio margen inferior
  (formulario de medicamento).

### `AppFormActions` (`AppFormActions.kt`)

```kotlin
AppFormActions(
    primaryText, onPrimaryClick, modifier, primaryEnabled, supportingText,
    secondaryIcon, secondaryContentDescription, secondaryContentColor,
    onSecondaryClick
)
```

- Se coloca al final del formulario, dentro del contenido desplazable.
- La acción principal es un `AppButton` `Large` que ocupa el ancho disponible.
- La acción secundaria es un cuadrado con icono a la derecha; úsala para
  acciones destructivas o de cierre, con `secondaryContentColor = error` si
  corresponde.
- `supportingText` avisa de campos pendientes; usa `bodySmall` y
  `onSurfaceVariant`.
- No inventes una fila de botones propia para guardar/editar/eliminar.

### `AppDropdownField` (`AppDropdownField.kt`)

```kotlin
AppDropdownField(value, options, onOptionSelected, modifier, placeholder, enabled)
```

- `Surface` con `shapes.medium`, `AppElevation` y `surface`.
- `TextField` de solo lectura y menú desplegable.

### `AppDatePickerField` (`AppDatePicker.kt`)

```kotlin
AppDatePickerField(
    value, onDateSelected, modifier, placeholder, enabled,
    isError, minDate, maxDate
)
```

- Campo de solo lectura que abre un `DatePickerDialog`.
- `minDate`/`maxDate` limitan las fechas seleccionables.

### `AppPhotoPicker` (`AppPhotoPicker.kt`)

```kotlin
AppPhotoPicker(
    photoUri, onPhotoSelected, modifier, enabled, size = 96.dp,
    placeholderIcon, photoDescriptionRes
)
```

- Recorte con `AppImageShape`, fondo `surfaceVariant`.
- Badge de edición de 26dp (solo si `enabled`) con icono de 13dp.
- `size` por defecto 96dp.
- Sin foto, delega en `AppImagePlaceholder` (mismo aspecto que las cards).

### `AppImagePlaceholder` (`AppImagePlaceholder.kt`)

```kotlin
AppImagePlaceholder(icon, contentDescription, modifier, size = 64.dp)
```

- Hueco común para imágenes sin foto: fondo `surfaceVariant`, recorte
  `AppImageShape` e icono `onSurfaceVariant` al 40 % del tamaño.
- Lo usan `AppPhotoPicker`, `DefaultPill()` (medicamentos) y `DefaultAvatar()`
  (perfiles), así que las cards y las fichas de solo lectura se ven igual.

## Solo lectura

### `AppReadOnlySection` (`AppReadOnlySection.kt`)

```kotlin
AppReadOnlySection(title, modifier, content)
```

- Sección sin card: título `titleLarge` en `primary`, 8dp, divisor de 1dp
  `outlineVariant` y 4dp hasta el contenido.
- Para separar secciones, el contenedor usa `Arrangement.spacedBy(AppSpacing.lg)`.

### `AppDetailRow` (`AppDetailRow.kt`)

```kotlin
AppDetailRow(label, value, modifier, stacked = false, showDivider = true)
```

- Etiqueta `bodyLarge` en `onSurfaceVariant` y valor 18sp/26sp Medium en
  `onSurface`, alineado a la derecha; alto mínimo 48dp.
- `stacked` coloca el valor debajo para textos largos (notas médicas).
- Si `value` es nulo o está en blanco, la fila no se muestra.

### `AppNoticeBlock` (`AppNoticeBlock.kt`)

```kotlin
AppNoticeBlock(icon, title, text, modifier)
```

- Bloque de aviso sin elevación: `secondaryContainer`/`onSecondaryContainer`,
  `shapes.medium` y padding 16dp; el icono va en `secondary`.
- Uso previsto: información que debe destacar, como las alergias de un perfil.

### `AppScrollWithFooter` (`AppScrollWithFooter.kt`)

```kotlin
AppScrollWithFooter(modifier, scrollState, footer, content)
```

- Contenido desplazable con el footer al final: si no llena la pantalla, las
  acciones se anclan abajo; si es más alto, se van con el scroll.
- Mide el footer con `onSizeChanged` para no asumir su alto con texto grande.
- Aplica `AppSpacing.md` de margen y separa el contenido con
  `AppSpacing.lg`; el footer llevará su propio `AppFormActions`.

## Cards

Todas usan `MaterialTheme.shapes.large` (20dp), `surface` y `AppElevation`.

### `BlankCard` (`BlankCard.kt`)

```kotlin
BlankCard(text, modifier)
```

- Estado vacío o mensaje centrado; 32dp (`AppSpacing.xxl`) de alto visual.
- Texto `bodyMedium` en `onSurfaceVariant`.

### `MedicationCard` (`MedicationCard.kt`)

```kotlin
MedicationCard(medication, accion, modifier, buttonText, buttonVariant)
```

- Imagen de 64dp con `AppImageShape`, nombre `titleMedium` y botón `Small`.
- Si no hay imagen, `DefaultPill()` con `AppImagePlaceholder`.

### `IntakeCard` (`IntakeCard.kt`)

```kotlin
IntakeCard(intake, visualState, onTake, modifier)
```

- Misma estructura que `MedicationCard`.
- Estado `PENDING` → botón `Primary`; `TAKEN` → `Secondary` deshabilitado;
  `DELAYED` → `Danger`.
- Metadatos de dosis y hora en `bodySmall` / `onSurfaceVariant`.

### `PerfilCard` (`PerfilCard.kt`)

```kotlin
PerfilCard(profile, gestionar, modifier, buttonText, buttonVariant)
```

- Imagen de 64dp con `AppImageShape`; si no hay, `DefaultAvatar()` con
  `AppImagePlaceholder`.
- Nombre `titleMedium` y botón `Small`.

## Estadísticas

Viven en `components/statistics/` y se usan en `StatisticsScreen` y en el
banner de Inicio.

### `StatisticsBanner` (`statistics/StatisticsBanner.kt`)

```kotlin
StatisticsBanner(summary, onClick, modifier)
```

- Card pulsable con icono, «Resumen semanal», «X de Y tomas» y porcentaje.
- Se oculta desde la pantalla si `summary.evaluated == 0`.

### `StatisticsPeriodSelector` (`statistics/StatisticsPeriodSelector.kt`)

```kotlin
StatisticsPeriodSelector(selectedPeriod, onPeriodSelected, modifier)
```

- Tres chips con el estilo de `WeekDayChipGroup`: `primary` seleccionado,
  `surface` no seleccionado y `AppElevation`.

### `StatisticsSummaryCard` (`statistics/StatisticsSummaryCard.kt`)

```kotlin
StatisticsSummaryCard(summary, modifier)
```

- Porcentaje en chip `primaryContainer`; conteos con punto de color
  (`tertiary`, `error`, `outline`).

### `WeeklyStatisticsChart` (`statistics/WeeklyStatisticsChart.kt`)

```kotlin
WeeklyStatisticsChart(days, modifier)
```

- Barras apiladas al 100 % con Compose puro, sin librerías.
- Cada barra tiene `contentDescription` con el día y los conteos.
- La leyenda usa `FlowRow` para envolver a fuente grande.

### `AdherenceLineChart` (`statistics/AdherenceLineChart.kt`)

```kotlin
AdherenceLineChart(points, title, modifier)
```

- Card con gráfico de líneas de adherencia (0–100 %), guías horizontales,
  etiquetas de fecha en los extremos y el centro, y puntos en la línea.
- La línea se dibuja con curvas suaves entre los puntos y un relleno suave
  debajo; los puntos sin tomas evaluadas dejan un hueco.
- La serie se recibe ya calculada (`AdherencePoint`) y los textos ya resueltos;
  el subtítulo indica que cada punto es una semana.
- Se usa en 30 y 90 días con puntos semanales; en 7 días se mantiene
  `WeeklyStatisticsChart`.

### `MedicationStatisticsCard` (`statistics/MedicationStatisticsCard.kt`)

```kotlin
MedicationStatisticsCard(statistics, modifier)
```

- Imagen o `DefaultPill` (con `AppImagePlaceholder`), nombre, dosis, «X de Y
  tomas», pendientes y porcentaje. Etiqueta «Archivado» cuando corresponde.

### `StatisticsRow` (`statistics/StatisticsRow.kt`)

```kotlin
StatisticsRow(label, description, onClick, modifier, icon = Icons.Rounded.BarChart)
```

- Fila pulsable con icono configurable (`BarChart` por defecto), etiqueta
  `bodyLarge` en `primary`, descripción `bodySmall` en `onSurfaceVariant` y
  chevron final.
- Se usa en las fichas de perfil y de medicamento para abrir las estadísticas y,
  con `Icons.Rounded.Archive`, para abrir los medicamentos archivados desde el
  perfil. Recibe los textos ya resueltos con `stringResource`.

### `StatisticsHistoryItem` (`statistics/StatisticsHistoryItem.kt`)

```kotlin
StatisticsHistoryItem(intake, status, modifier)
```

- Hora, medicamento, dosis y chip de estado con colores semánticos.

## Ajustes

### `SettingsCard` (`settings/SettingsCard.kt`)

```kotlin
SettingsCard(title, modifier, content)
```

- Card `large`, padding 16dp; título opcional `titleMedium` Bold en `primary`.

### `SettingsToggleItem` (`settings/SettingsToggleItem.kt`)

```kotlin
SettingsToggleItem(label, checked, onCheckedChange, modifier, enabled)
```

- Fila con etiqueta `bodyLarge` Medium y `Switch`.

### `SettingsRadioItem` (`settings/SettingsRadioItem.kt`)

```kotlin
SettingsRadioItem(label, selected, onClick, modifier, description)
```

- Fila seleccionable con `RadioButton`; toda la fila es clicable y cumple el
  mínimo de 48dp.
- Se usa dentro de `Column(Modifier.selectableGroup())`; etiqueta `bodyLarge` y
  descripción opcional `bodyMedium` en `onSurfaceVariant`.
- `SettingsToggleItem` se mantiene para opciones binarias; este componente es
  para grupos exclusivos.

### `SettingsLinkItem` (`settings/SettingsLinkItem.kt`)

```kotlin
SettingsLinkItem(label, onClick, modifier, enabled)
```

- Texto `bodyMedium` en `primary`; deshabilitado en `onSurfaceVariant` al 50%.
- Ancho completo, clicable y con 4dp verticales.

## Horarios

### `WeekDayChipGroup` (`WeekDayChipGroup.kt`)

```kotlin
WeekDayChipGroup(selectedDays, onSelectionChange, enabled)
```

- Siete chips de igual ancho, proporción 1:1, separados 8dp.
- `shapes.medium`, `AppElevation`; seleccionado `primary`/`onPrimary`,
  no seleccionado `surface`/`onSurface`; deshabilitado alpha 0.5.
- Etiquetas cortas con `WeekDay.shortNameRes()`.

### `TimeListEditor` (`timeeditor/TimeListEditor.kt`)

```kotlin
TimeListEditor(times, onAddClick, onRemoveTime, onUpdateTime, enabled)
```

- Columna con 8dp arriba y 16dp entre filas; abre el selector de hora.
- Añade `AddTimeItem` (botón `Tonal` `Large`) si está habilitado.

### `TimeItem` (`timeeditor/TimeItem.kt`)

```kotlin
TimeItem(time, enabled, onClick, onRemove)
```

- `Surface` con `shapes.medium`, `AppElevation` y `surface`.
- Hora en `bodyLarge`; eliminar con `IconButton` e icono en `error`.
- Toda la fila es pulsable (`onClickLabel = edit_time`); el `IconButton` asegura
  un área táctil de 48dp.

### `AddTimeItem` (`timeeditor/AddTimeItem.kt`)

```kotlin
AddTimeItem(onClick)
```

- `AppButton` `Tonal` `Large` con icono `Add`.

## Reglas de composición

- Un componente recibe estado y callbacks; no accede a ViewModels, repositorios
  ni lógica de dominio.
- Los textos se resuelven en la pantalla con `stringResource` y se pasan ya
  resueltos al componente.
- Si un componente necesita un color, una forma o un espaciado nuevo, usa los
  tokens del tema; no añadas valores locales.
- Todo componente nuevo lleva KDoc en español y un `@Preview` como mínimo.
- Si un componente se repite en dos pantallas, extráelo a `ui/components` en vez
  de duplicarlo.
- No añadas un `Scaffold` a un componente de contenido: el `Scaffold` pertenece
  a la pantalla.
