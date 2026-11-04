# Tokens de diseño

Copia de referencia de los valores definidos en `ui/theme`. El código es la
fuente de verdad: si un valor cambia, actualiza también esta tabla.

## Color

### Cómo elegir un rol

| Rol | Uso | Evitar |
|---|---|---|
| `primary` | Marca, títulos de sección, acciones principales, icono activo de la barra inferior. | Usarlo como fondo de bloques grandes de información. |
| `onPrimary` | Texto e iconos sobre `primary`. | Texto secundario. |
| `primaryContainer` / `onPrimaryContainer` | Fondos suaves: chip seleccionado, avatar por defecto, FAB de perfiles. | Sustituir a `surface` en cards. |
| `secondary` | Naranja de aviso; hoy se usa sobre todo como base de `secondaryContainer`. | Decoración o texto largo. |
| `secondaryContainer` / `onSecondaryContainer` | Botón de una toma ya realizada (`ButtonVariant.Secondary`). | Estado pendiente. |
| `tertiary` / `tertiaryContainer` | Verde de confirmación; uso puntual. | Acciones principales. |
| `error` / `onError` | Acciones destructivas (`ButtonVariant.Danger`) y toma retrasada. | Texto de error sobre `surface` sin contraste. |
| `errorContainer` / `onErrorContainer` | Fondos de error puntuales. | Sustituir a `error` en botones. |
| `surface` | Fondo de cards, campos, chips no seleccionados y filas de horas. | Fondo general de pantalla. |
| `surfaceVariant` | Campos deshabilitados, fondo del selector de foto y separadores suaves. | Fondo de cards. |
| `onSurface` | Texto principal y valores de lectura. | Texto secundario. |
| `onSurfaceVariant` | Texto secundario, metadatos, descripciones y enlaces deshabilitados. | Texto principal. |
| `outline` / `outlineVariant` | Bordes y divisores finos. | Texto. |
| `background` | Fondo general y barra superior. | Fondo de cards. |
| `scrim` | Velo de diálogos y hojas. | Fondos normales. |
| `inverseSurface` / `inverseOnSurface` | Snackbars y superficies invertidas. | Cards normales. |

Reglas:

- `Color(0x...)` vive únicamente en `Color.kt`; en pantallas y componentes usa
  `MaterialTheme.colorScheme`.
- El modo dinámico está desactivado (`dynamicColor = false` en
  `PillRemindTheme`).
- No inventes un color para un estado nuevo: reutiliza un rol existente o
  añádelo al tema y documenta su significado.

### Marca

| Nombre | Hex | Uso |
|---|---|---|
| `BrandPurple` | `#32138D` | Púrpura de marca: acciones principales y títulos. |
| `ActionOrange` | `#FF6F00` | Naranja de aviso. |
| `SuccessGreen` | `#81C784` | Verde de confirmación. |
| `NeutralGray` | `#5F5F5F` | Texto secundario. |
| `TextBlack` | `#1A1A1A` | Texto principal en claro. |
| `SurfaceWhite` | `#FFFFFF` | Superficies en claro. |
| `BackgroundOff` | `#F7F7F6` | Fondo general en claro. |

### Tema claro

| Rol | Hex |
|---|---|
| `primary` | `#32138D` |
| `onPrimary` | `#FFFFFF` |
| `primaryContainer` | `#EDE7F6` |
| `onPrimaryContainer` | `#32138D` |
| `secondary` | `#FF6F00` |
| `onSecondary` | `#2B1700` |
| `secondaryContainer` | `#FFF3E0` |
| `onSecondaryContainer` | `#6B2E00` |
| `tertiary` | `#81C784` |
| `onTertiary` | `#00391C` |
| `tertiaryContainer` | `#E8F5E9` |
| `onTertiaryContainer` | `#1B5E20` |
| `background` | `#F7F7F6` |
| `onBackground` | `#1A1A1A` |
| `surface` | `#FFFFFF` |
| `onSurface` | `#1A1A1A` |
| `surfaceVariant` | `#EEEEEE` |
| `onSurfaceVariant` | `#5F5F5F` |
| `error` | `#B3261E` |
| `onError` | `#FFFFFF` |
| `errorContainer` | `#F9DEDC` |
| `onErrorContainer` | `#410E0B` |
| `outline` | `#7A7A7A` |
| `outlineVariant` | `#D9D9D7` |
| `scrim` | `#000000` |
| `inverseSurface` | `#303030` |
| `inverseOnSurface` | `#F5F5F5` |
| `inversePrimary` | `#BB9EFF` |
| `surfaceDim` | `#DEDEDC` |
| `surfaceBright` | `#FFFFFF` |
| `surfaceContainerLowest` | `#FFFFFF` |
| `surfaceContainerLow` | `#F7F7F6` |
| `surfaceContainer` | `#F1F1EF` |
| `surfaceContainerHigh` | `#EAEAE8` |
| `surfaceContainerHighest` | `#E2E2E0` |

### Tema oscuro

| Rol | Hex |
|---|---|
| `primary` | `#BB9EFF` |
| `onPrimary` | `#1E0070` |
| `primaryContainer` | `#32138D` |
| `onPrimaryContainer` | `#EDE7F6` |
| `secondary` | `#FFB74D` |
| `onSecondary` | `#4D2C00` |
| `secondaryContainer` | `#704000` |
| `onSecondaryContainer` | `#FFDDB3` |
| `tertiary` | `#B9F6CA` |
| `onTertiary` | `#00391C` |
| `tertiaryContainer` | `#00522C` |
| `onTertiaryContainer` | `#D7FFD9` |
| `background` | `#121212` |
| `onBackground` | `#EEEEEE` |
| `surface` | `#1E1E1E` |
| `onSurface` | `#EEEEEE` |
| `surfaceVariant` | `#2C2C2C` |
| `onSurfaceVariant` | `#BDBDBD` |
| `error` | `#F2B8B5` |
| `onError` | `#601410` |
| `errorContainer` | `#8C1D18` |
| `onErrorContainer` | `#F9DEDC` |
| `outline` | `#8A8A8A` |
| `outlineVariant` | `#444444` |
| `scrim` | `#000000` |
| `inverseSurface` | `#E6E6E6` |
| `inverseOnSurface` | `#303030` |
| `inversePrimary` | `#32138D` |
| `surfaceDim` | `#121212` |
| `surfaceBright` | `#3A3A3A` |
| `surfaceContainerLowest` | `#0E0E0E` |
| `surfaceContainerLow` | `#1A1A1A` |
| `surfaceContainer` | `#242424` |
| `surfaceContainerHigh` | `#2C2C2C` |
| `surfaceContainerHighest` | `#333333` |

## Tipografía

Familia: **Lexend** (`FontFamily.kt`). Todos los roles usan Lexend.

| Rol | Peso | Tamaño | Interlineado | Tracking |
|---|---|---|---|---|
| `displayLarge` | Normal | 57sp | 64sp | -0.25sp |
| `displayMedium` | Normal | 45sp | 52sp | 0sp |
| `displaySmall` | Normal | 36sp | 44sp | 0sp |
| `headlineLarge` | SemiBold | 32sp | 40sp | 0sp |
| `headlineMedium` | SemiBold | 28sp | 36sp | 0sp |
| `headlineSmall` | SemiBold | 24sp | 32sp | 0sp |
| `titleLarge` | Bold | 18sp | 24sp | 0sp |
| `titleMedium` | SemiBold | 16sp | 24sp | 0.1sp |
| `titleSmall` | SemiBold | 14sp | 20sp | 0.1sp |
| `bodyLarge` | Normal | 16sp | 24sp | 0.5sp |
| `bodyMedium` | Normal | 14sp | 20sp | 0.25sp |
| `bodySmall` | Normal | 12sp | 16sp | 0.4sp |
| `labelLarge` | Bold | 14sp | 20sp | 0.1sp |
| `labelMedium` | Medium | 12sp | 16sp | 0.5sp |
| `labelSmall` | Medium | 11sp | 16sp | 0.5sp |

### Cómo elegir un rol

| Rol | Uso | Evitar |
|---|---|---|
| `titleLarge` | Títulos de sección (`AppFormSection`, `AppInfoCard`) y título de la top bar. | Texto de párrafo. |
| `titleMedium` | Título de card (`MedicationCard`, `IntakeCard`, `PerfilCard`, `SettingsCard`). | Títulos de sección. |
| `titleSmall` | Subtítulos y bloques secundarios. | Texto de lectura. |
| `bodyLarge` | Texto de lectura, etiquetas de ajustes, horas. | Metadatos pequeños. |
| `bodyMedium` | Descripciones, enlaces de ajustes y estado vacío. | Texto principal de lectura. |
| `bodySmall` | Metadatos (dosis y hora) y avisos secundarios. | Texto principal o descripciones largas. |
| `labelLarge` | Etiqueta de campo y botón mediano. | Párrafos. |
| `labelMedium` | Chips y botón pequeño. | Texto de lectura. |
| `labelSmall` | Etiquetas muy compactas. | Texto de lectura. |
| `headline*` / `display*` | Reservados; no hay uso actual. | Pantallas normales. |

Lectura aumentada: `AppInfoRow` y `AppReadOnlyField` usan `bodyLarge` con
`fontSize = 18.sp`, `lineHeight = 26.sp` y peso Medium.

No reduzcas el tamaño para que un texto quepa: usa `maxLines` + elipsis en
títulos y permite que las descripciones envuelvan.

## Formas

`AppShapes` en `Shapes.kt`:

| Token | Valor | Uso |
|---|---|---|
| `extraSmall` | 4dp | Reservado. |
| `small` | 8dp | Reservado. |
| `medium` | 12dp | Campos, botones, chips, filas de horas, desplegables. |
| `large` | 20dp | Cards de contenido, info, ajustes y estados vacíos. |
| `extraLarge` | 28dp | Hojas y contenedores grandes. |
| `AppImageShape` | 14dp | Fotos, avatares y miniaturas. |

No escribas `RoundedCornerShape(...)` en pantallas o componentes: usa
`MaterialTheme.shapes` o `AppImageShape`. Si necesitas una forma nueva, añádela
a `Shapes.kt` y documenta su uso.

## Espaciado

`AppSpacing` en `Spacing.kt`:

| Token | Valor | Uso |
|---|---|---|
| `xxs` | 4dp | Huecos mínimos, separación interna de iconos, borde de fila de hora. |
| `xs` | 8dp | Etiqueta → campo, chips, controles relacionados. |
| `sm` | 12dp | Entre cards de una lista, entre campos, relleno vertical de cards de lista. |
| `md` | 16dp | Margen de pantalla, relleno de cards, huecos entre bloques. |
| `lg` | 20dp | Entre secciones de formulario (`ProfileScreen`). |
| `xl` | 24dp | Bajo la foto y entre bloques grandes del formulario de medicamento. |
| `xxl` | 32dp | Alto visual de un estado vacío. |

Excepciones que se mantienen como literales: 6dp de `AppInfoRow`, 10dp de los
bloques de estado de `SettingsScreen`, paddings internos de `AppButton`
(6/12/20/28), tamaños de iconos/imágenes/botones y bordes de 1/2dp.

## Elevación

`AppElevation` = **4dp** (`Elevation.kt`). Única elevación de la app: cards,
campos, chips, filas de horas, botones y FAB. No uses otra elevación ni sombras
propias.
