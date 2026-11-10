---
name: PillRemind Mockups
description: Úsala antes de crear o revisar un mockup HTML de PillRemind: reproduce los componentes, tokens, textos y estados reales de la app y evita inventar campos, badges o botones.
---

# Mockups de PillRemind

## Cuándo usarla

- Antes de crear o modificar un mock HTML de una pantalla, flujo o componente.
- Antes de enseñar una propuesta visual.
- Al revisar un mock existente para comprobar que sigue pareciendo la app.

## Regla de oro

Un mock **no diseña** la app: la refleja. Si un elemento no existe en
`ui/components` o en una pantalla real, no lo inventes. Usa el componente o
patrón más cercano y anótalo. El código manda; el mock se adapta a él.

## Fuentes de verdad

| Tema | Archivo |
|---|---|
| Sistema visual | `.opencode/skills/pillremind-design/SKILL.md` |
| Color y tema | `PillRemind/app/src/main/java/com/andres/pillremind/ui/theme/` |
| Componentes | `PillRemind/app/src/main/java/com/andres/pillremind/ui/components/` |
| Pantallas | `PillRemind/app/src/main/java/com/andres/pillremind/ui/screens/` |
| Textos | `PillRemind/app/src/main/res/values/strings.xml` |
| Iconos | `PillRemind/app/src/main/res/drawable/ic_*.xml` (Material Symbols Rounded) |
| Capturas reales | `doc/img/` (comprueba que estén al día) |
| Mockups locales | `doc/mockups/` (ignorado por Git; no se versiona) |

Si el código y el mock discrepan, corrige el mock. No cambies producción para
que encaje con un mock salvo que el usuario lo pida expresamente.

## Flujo

1. Lee `pillremind-design` y el código real de la pantalla o componente que
   vas a representar.
2. Haz una lista de cada elemento y su componente real: `AppFormSection`,
   `AppFormField`, `AppTextField`, `AppDetailRow`, `AppNoticeBlock`,
   `AppButton`, `AppFormActions`, etc.
3. Si falta un componente, usa el patrón real más cercano:
   - hoja inferior → `HistoryFiltersSheet` + `ModalBottomSheet`;
   - diálogo → `AlertDialog`;
   - horarios → `ScheduleEditor` + `TimeListEditor`;
   - aviso → `AppNoticeBlock`.
4. Copia los tokens exactos. No inventes colores, sombras, radios ni tamaños.
5. Monta el HTML con una **capa de componentes** (`app-form-section`,
   `app-form-field`, `app-text-field`, `app-button`, …) que replique Compose.
   No uses clases ad-hoc para cada bloque.
6. Incluye los estados reales: nuevo/existente/incompleto, claro/oscuro,
   texto grande y ancho de móvil (412 dp).
7. Abre el mock, haz capturas y compáralas con la pantalla real. Corrige las
   diferencias antes de enseñarlo.
8. Guarda el mock en `doc/mockups/` y **no lo commitees**. Es documentación
   local, no código de la app.

## Medidas que no se negocian

- **Tipografía:** Lexend. `bodyLarge` 16/24, `bodyMedium` 14/20,
  `bodySmall` 12/16 solo para metadatos, `labelLarge` 14/20 bold,
  `titleLarge` 18/24 bold, `titleMedium` 16/24 semibold.
- **AppFormSection:** título 18/24 bold en `primary`, 12 dp hasta el contenido.
- **AppFormField:** etiqueta 14/20 bold en `onSurface`, 8 dp hasta el campo.
- **AppTextField / AppDropdownField:** superficie `surface`, radio 12 dp,
  sombra 4 dp, alto mínimo 56 dp, texto 16/24, padding horizontal 16 dp.
  Nunca un campo con borde de 1 px.
- **AppDetailRow:** etiqueta 16/24 en `onSurfaceVariant`, valor 18/26 medium
  en `onSurface`, alto mínimo 48 dp, divisor 1 dp. A partir de
  `fontScale >= 1.3` se apila.
- **AppNoticeBlock:** `secondaryContainer`, radio 12 dp, padding 16 dp,
  icono 22 dp, título 14/20 bold, texto 16/24.
- **AppButton:** radio 12 dp; `Primary`/`Tonal`/`Ghost`; `Small` 110×40 con
  12/16, `Medium` 160 con 14/20, `Large` ancho completo con 14/20 y 52 dp;
  elevación 4 dp. Las acciones secundarias usan `Tonal`; `Ghost` solo inline.
  No inventes enlaces de texto.
- **AppFormActions:** acción principal `Large` + texto de apoyo 12/16.
  No hagas un pie con borde y sombra propios.
- **Checkbox:** 20 dp visual, 48 dp de área táctil, Material 3 y fila
  `toggleable`. No un checkbox de 21 px pintado a mano.
- **SettingsToggleItem:** etiqueta 16/24 medium + `Switch` de Material 3.
- **TimeListEditor:** `TimeItem` de 56 dp con superficie, 12 dp y 4 dp de
  sombra, y `AddTimeItem` como `AppButton Tonal Large`. No chips de horas.
- **ScheduleEditor:** todas las periodicidades (días de la semana, cada N días,
  mensual por día, mensual por día de la semana, ciclo y toma única). No lo
  reduzcas a «cada día / cada 2 / cada 3 / cada semana» salvo que se pida.
- **ModalBottomSheet:** fondo `background`, radio superior 28 dp, tirador,
  patrón de `HistoryFiltersSheet` y campos con `AppFormField`.
- **AlertDialog:** radio 28 dp, título 24/32, texto 14/20 y acciones
  `TextButton`.
- **Cards:** radio 20 dp, `surface`, 4 dp de elevación y 16 dp de padding.
- **Espaciado:** 4/8/12/16/20/24/32 dp; margen de pantalla 16 dp.
- **Color:** solo roles del tema. Claro y oscuro. No uses blanco o negro fijos.
- **Iconos:** Material Symbols Rounded, los mismos `ic_*.xml` que usa la app.
  No uses Material Icons, emojis ni iconos de otra familia.
- **Texto visible:** siempre desde `strings.xml`; en el mock, copia los textos
  reales, no inventes etiquetas.

## Errores que ya se cometieron y no hay que repetir

- Usar los colores correctos pero **inventar los componentes**. El mock parecía
  una web genérica, no PillRemind.
- Usar 10,5–12,5 px en nombres, dosis, estados y botones. La app usa 16/18/14.
- Inventar badges «Nuevo / Ya existe / Falta pauta». La app usa
  `AppNoticeBlock` y texto de estado.
- Pintar un checkbox de 21 px en lugar de usar el `Checkbox` de Material 3 con
  48 dp de área táctil.
- Usar enlaces «Editar / Ver ficha» en lugar de `AppButton` (`Tonal`/`Ghost`).
- Inventar una hoja inferior con inputs de borde y un switch propio en lugar de
  `ModalBottomSheet` + `ScheduleEditor` + `SettingsToggleItem`.
- Poner títulos de sección en negro en lugar de `AppFormSection` (18 dp bold
  en `primary`).
- Añadir un resumen que no se había pedido.
- Reducir la pauta a cuatro opciones cuando la app ya tiene `ScheduleEditor`
  con todas las periodicidades.

## Checklist antes de entregar

- [ ] Cada elemento está mapeado a un componente o patrón real.
- [ ] No hay colores, sombras, radios ni bordes inventados.
- [ ] El texto de lectura es 16 dp; no hay 10–12 px en contenido principal.
- [ ] Se ve bien en claro y oscuro, a 412 dp y con texto grande.
- [ ] Las capturas se compararon con la pantalla real.
- [ ] El mock está en `doc/mockups/` y no se ha commiteado.
- [ ] No se tocó código de producción para que encajara el mock.
