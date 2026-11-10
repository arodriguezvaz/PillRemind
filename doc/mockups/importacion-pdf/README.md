# Mock · Importar medicamentos desde PDF

Mock HTML de la pantalla de revisión de la importación de medicamentos desde
la hoja de medicación activa en PDF.

No es código de la aplicación: reproduce la pantalla y las interacciones con
los tokens y componentes reales de PillRemind (Lexend, colores, formas,
espaciado, elevación, `AppFormField`, `AppTextField`, `AppDetailRow`,
`AppNoticeBlock`, `AppButton`, `AppFormActions`, `SettingsToggleItem`,
`TimeListEditor` y `ModalBottomSheet`).

## Abrir

```bash
xdg-open doc/mockups/importacion-pdf/index.html
```

También se puede servir en local:

```bash
cd doc/mockups/importacion-pdf
python3 -m http.server 8080
```

y abrir `http://localhost:8080`.

## Qué muestra

- Cuatro medicamentos de ejemplo: dos nuevos, uno que ya existe y uno sin
  pauta.
- Los nuevos aparecen marcados y con el estado «Se añadirá».
- Los que ya existen aparecen sin marcar, con «Ya existe», y no cuentan para
  el botón de guardar.
- Si el existente tiene otra dosis o pauta, se avisa y se ofrece abrir su
  ficha. La importación no modifica medicamentos existentes.
- El que no tiene pauta queda bloqueado hasta completarla.
- «Editar» abre una hoja inferior con nombre, dosis, unidad, periodicidad,
  horas y tratamiento crónico.
- El pie se actualiza según lo que se marque; no hay bloque de resumen.
- El conmutador de la cabecera permite ver el mock en claro y en oscuro.

## Componentes

- `AppDetailRow` para la dosis, la pauta y el tratamiento de cada medicamento.
- `AppFormField` + `AppTextField`, `AppDropdownField` y `AppDatePickerField`
  para los campos.
- `AppNoticeBlock` para los avisos de duplicado y de pauta incompleta.
- `AppButton` y `AppFormActions` para las acciones.
- `ScheduleEditor` con todas las periodicidades (días de la semana, cada N días, mensual, ciclo y toma única) y `TimeListEditor` en la hoja de edición.
- `SettingsToggleItem` para el tratamiento crónico.
- `ModalBottomSheet` siguiendo el patrón de `HistoryFiltersSheet`.

## Atajos

- `index.html#duplicates`: abre desplazado a la zona de duplicados.
- `index.html?theme=dark`: abre en modo oscuro.
- `index.html?sheet=ryaltr`: abre directamente la hoja de edición de
  RYALTRIS.
- `index.html?sheet=ryaltr&instant=1`: igual, pero sin animaciones; se usa
  para las capturas.

## Capturas

- `revision.png`: parte superior de la revisión.
- `duplicados.png`: duplicado y medicamento sin pauta.
- `edicion.png`: hoja de edición.
- `oscuro.png`: variante oscura.

## Notas

- El checkbox de un medicamento que ya existe abre el aviso para añadirlo
  igualmente. No hay un botón aparte en la card; el comportamiento por
  defecto es no crearlo.
- Los botones de las cards son anchos y no llevan icono.
- El PDF real no se incluye en el repositorio. Los datos de ejemplo son
  ficticios.
- Las fuentes Lexend se copian en `fonts/` para que el mock sea
  autocontenido.
