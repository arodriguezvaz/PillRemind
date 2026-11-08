<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="doc/img/logo-oscuro.png">
    <img src="doc/img/logo.png" width="132" alt="Logotipo de PillRemind: una pastilla semirrellena, icono de la app">
  </picture>
</p>

<h1 align="center">PillRemind</h1>

<p align="center">
  <a href="https://github.com/arodriguezvaz/PillRemind/releases/latest"><img src="https://img.shields.io/github/v/release/arodriguezvaz/PillRemind?label=versi%C3%B3n&color=2ea44f" alt="Última versión"></a>
</p>

<p align="center">
  Recordatorio de medicación local para Android 8.0 o superior. Sin cuentas, sin
  sincronización y sin nube: los perfiles, medicamentos y tomas se guardan en el móvil.
</p>

## Descarga

La aplicación se distribuye como APK desde las
[releases de GitHub](https://github.com/arodriguezvaz/PillRemind/releases/latest).
No está publicada en Google Play.

- Requiere Android 8.0 (API 26) o superior.
- Descarga `PillRemind-x.y.z.apk` y ábrelo. Android pedirá permiso para instalar
  apps de fuentes desconocidas y puede mostrar un aviso de Play Protect.
- Las actualizaciones se avisan desde la propia app y se instalan sobre la versión
  anterior conservando los datos.

**Guía paso a paso, con capturas:**
[Manual de descarga e instalación](doc/docs/manual-instalacion.md)

## Qué hace

- **Perfiles**: medicación separada para varias personas en el mismo móvil.
- **Medicamentos**: dosis, unidad, foto y pauta por días, intervalos o ciclos.
- **Recordatorios**: notificación a la hora de cada toma, con acción para marcarla.
- **Historial y adherencia**: búsqueda y filtros de tomas, resumen de 7, 30 y 90 días.
- **Copia de seguridad local**: exportación e importación con contraseña opcional.

**Guía de uso, con capturas:** [Manual de uso](doc/docs/manual-uso.md)

## Privacidad

Todos los datos se guardan en una base de datos Room del dispositivo. La única
conexión a internet es la comprobación diaria de actualizaciones contra GitHub
Releases, que no envía datos de salud ni identificadores personales.

## Desarrollo

Android con Kotlin, Jetpack Compose, Hilt, Room y WorkManager. El APK de release se
compila, firma y publica con GitHub Actions.

**Detalles técnicos, compilación y publicación:**
[Guía de desarrollo](doc/docs/desarrollo.md)

## Autoría y licencia

Proyecto personal de [Andrés Rodríguez Vázquez](https://github.com/arodriguezvaz),
nacido como proyecto de fin de ciclo DAM y mantenido desde entonces. Se distribuye
bajo licencia MIT; consulta [LICENSE](LICENSE).
