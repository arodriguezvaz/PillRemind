<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="doc/img/logo-oscuro.png">
    <img src="doc/img/logo.png" width="132" alt="Logotipo de PillRemind: una pastilla semirrellena, icono de la app">
  </picture>
</p>

<h1 align="center">PillRemind</h1>

<p align="center">
  <a href="https://github.com/arodriguezvaz/PillRemind/releases/latest"><img src="https://img.shields.io/github/v/release/arodriguezvaz/PillRemind?label=versi%C3%B3n&color=2ea44f" alt="Última versión"></a>
  <a href="https://github.com/arodriguezvaz/PillRemind/releases"><img src="https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/arodriguezvaz/PillRemind/stats/estadisticas.json" alt="Descargas APK"></a>
</p>

<p align="center">
  Recordatorio de medicación local para Android 8.0 o superior. Sin cuentas, sin
  sincronización y sin nube: los perfiles, medicamentos y tomas se guardan en el móvil.
</p>

## Descarga

La aplicación se distribuye como APK desde las
[releases de GitHub](https://github.com/arodriguezvaz/PillRemind/releases/latest)
y desde la [web oficial](https://pillremind.arodriguezvaz.duckdns.org/descargar.html).
No está publicada en Google Play.

- Requiere Android 8.0 (API 26) o superior.
- Descarga `PillRemind-x.y.z.apk` y ábrelo. Android pedirá permiso para instalar
  apps de fuentes desconocidas y puede mostrar un aviso de Play Protect.
- Las actualizaciones se avisan desde la propia app y se instalan sobre la versión
  anterior conservando los datos.
- El escaneo de medicamentos usa ML Kit a través de Google Play Services; el resto
  de la app funciona aunque no esté disponible.

**Guía paso a paso, con capturas:**
[Manual de descarga e instalación](doc/docs/manual-instalacion.md)

## Qué hace

- **Perfiles**: medicación separada para varias personas en el mismo móvil.
- **Medicamentos**: dosis, unidad, foto y pauta por días, intervalos o ciclos.
- **Escaneo de medicamentos**: fotografía la caja y el código para leer el CN,
  consultar CIMA y rellenar la ficha. Si CIMA no encuentra el producto, la app
  puede leer el texto de la caja con OCR.
- **Importación desde PDF**: lee la hoja de medicación y propone los medicamentos
  para revisarlos antes de guardarlos.
- **Recordatorios**: notificación a la hora de cada toma, con acción para marcarla.
- **Historial y adherencia**: búsqueda y filtros de tomas, resumen de 7, 30 y 90 días
  e informe de adherencia en PDF.
- **Copia de seguridad local**: exportación e importación con contraseña opcional.

**Guía de uso, con capturas:** [Manual de uso](doc/docs/manual-uso.md)

**Detalle técnico del escaneo:** [OCR, códigos y CIMA](doc/docs/escaneo.md).

## Desarrollo

Android con Kotlin, Jetpack Compose, Hilt, Room y WorkManager. El APK de release se
compila, firma y publica con GitHub Actions.

**Detalles técnicos, compilación y publicación:**
[Guía de desarrollo](doc/docs/desarrollo.md)

## Legal

PillRemind es una herramienta de apoyo para recordar la medicación. No es un
producto sanitario, no emite diagnósticos ni recomendaciones médicas y no sustituye
la consulta con un profesional sanitario.

El escaneo, el OCR y la importación desde PDF son ayudas y pueden fallar. Revisa
siempre el nombre, la dosis y las horas antes de guardar. Ante cualquier duda,
consulta el prospecto o a tu médico o farmacéutico.

La información legal completa está publicada en la web:

- [Política de privacidad](https://pillremind.arodriguezvaz.duckdns.org/privacidad.html)
- [Términos de uso](https://pillremind.arodriguezvaz.duckdns.org/terminos.html)
- [Sección legal](https://pillremind.arodriguezvaz.duckdns.org/legal.html)

El código se distribuye bajo licencia MIT y la aplicación se ofrece «tal cual», sin
garantías de disponibilidad o funcionamiento ininterrumpido.

## Autoría y licencia

Proyecto personal de [Andrés Rodríguez Vázquez](https://github.com/arodriguezvaz),
nacido como proyecto de fin de ciclo DAM y mantenido desde entonces. Se distribuye
bajo licencia MIT; consulta [LICENSE](LICENSE).
