# Catálogo de componentes

Todos viven en `PillRemind/app/src/main/java/com/andres/pillremind/ui/components/`
(salvo los de ajustes, en `components/settings/`, y los de horarios, en
`components/timeeditor/`). Reutiliza estos componentes antes de crear uno nuevo.
Los textos que reciben son siempre cadenas ya resueltas con `stringResource`.

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

### `AppReadOnlyField` (`AppReadOnlyField.kt`)

```kotlin
AppReadOnlyField(label, value, modifier, valueColor)
```

- Para modo lectura: etiqueta `labelLarge` Medium y valor `bodyLarge` a 18sp/26sp
  Medium. No simula un input deshabilitado.

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
- `size` por defecto 96dp; en las cards se usa 64dp.

## Cards

Todas usan `MaterialTheme.shapes.large` (20dp), `surface` y `AppElevation`.

### `AppInfoCard` (`AppInfoCard.kt`)

```kotlin
AppInfoCard(title, modifier, content)
```

- Título `titleLarge` en `primary`, divisor de 1dp `outlineVariant`, padding 16dp.
- Contenido típico: filas `AppInfoRow`.

### `AppInfoRow` (`AppInfoRow.kt`)

```kotlin
AppInfoRow(label, value, modifier)
```

- Etiqueta en `onSurfaceVariant` y valor en `onSurface`, ambos Medium a 18sp/26sp.
- Si `value` es nulo o está en blanco, no se muestra.

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
- Si no hay imagen, `DefaultPill()` con `primaryContainer`.

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

- Imagen de 64dp con `AppImageShape`; si no hay, `DefaultAvatar()`.
- Nombre `titleMedium` y botón `Small`.

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
