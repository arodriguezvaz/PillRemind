
# Codificación y Pruebas

> **Nota:** este documento describe una versión anterior de la aplicación, con
> Supabase, autenticación y sincronización en la nube. La versión actual es
> local-only y guarda todos los datos en Room en el dispositivo. Se conserva como
> documentación histórica.

## Instalación del entorno de desarrollo

Para la instalación del entorno de desarrollo solo es necesario tener Android Studio y descargar el repositorio. Para probar la ejecución simplemente también se puede instalar la APK en un teléfono móvil o emulador con Android.

## Prototipos
### Prototipo 1
- Fecha de entrega: 21/4/26
- Funcionalidades implementadas:
    - Navegación anidada
    - Creación de perfiles en la base de datos local
    - Establecer perfil activo guardando el id usando DataStore para que no sea necesario pasar el id de perfil cada vez que se navega a una pestaña.
    - Recuperar la lista de perfiles con Flow
    - Recuperar el perfil a partir del id almacenado en DataStore
- Observaciones: la implementación de Room y de la navegación anidada me llevó más de lo que pensaba, sobre todo por el hecho de intentar mantener los mandatos de Clean Architecture. En el apartado de referencias se aportan todos los enlaces que me sirvieron para aprender estas tecnologías.
- Innovación:

    Para poder realizar el proyecto tuve que aprender la mayoría, pues Compose no es un framework que aprendiésemos en el ciclo.
    - Implementación de Room en Compose manteniendo los conceptos de Clean Architecture
    - Implementación de DataStore para almacenar el id del perfil seleccionado

- [Vídeo do prototipo](https://drive.google.com/file/d/142_gD4ye7jSW4_KM_gCqqgqEX95Fz5Hn/view?usp=sharing)
- [Ubicación da Apk](doc/apks)

### Prototipo 2
- Fecha de entrega: 6/5/26
- Funcionalidades implementadas:
    - Creación de medicamentos
    - Ver medicamentos
    - Editar medicamentos
    - Vistas de home y medicamentos con listas de medicamentos (al iniciar la app desde 0 parece que home no muestra los medicamentos)
    - Implementación de Hilt para facilitar la inyección de dependencias
    - Recuperar el perfil a partir del id almacenado en DataStore ahora con Hilt
    - Mejoras en la UI generales (fuente, colores, formularios, fab de añadir medicamento)
- Observaciones: la implementación de Hilt facilitó mucho la creación de nuevas entidades de la base de datos. Como son todas tecnologías nuevas me está llevando más tiempo del que esperaba implementarlas.
- Innovación:
    - Implementación de Bind
    - AlarmManager y solicitud de permisos
    - Creación y edición de medicamentos con sus correspondientes horarios

- [Vídeo do prototipo](https://drive.google.com/file/d/1_-ZXcwopkIS8GfKErutMvkEQoCRwyhbK/view?usp=sharing)
- [Ubicación da Apk](doc/apks)

### Prototipo 3
- Data de entrega: 2/6/26
- Funcionalidades implementadas:
    - Creación de tomas mediante WorkManager, que agora tamén crea cada día as tomas do día e as anteriores se non se crearon por algún erro.
    - Rexistro de toma dende a notificación e dende a app.
    - Implementación de Supabase, o que implica o proceso de autenticación e a sincronización.
    - Vistas de tomas.
    - Pestana de axustes con interruptor de modo escuro.
    - Bloqueo de creación de perfís en función do plan do usuario.
    - Cambios visuais no formulario de creación de usuario.
    - Busca de medicamentos na vista de medicamentos.
    - Engadir imaxe de perfil.
- Innovación:
    - Implementación de Supabase.
    - Implementación dun WorkManager.
    - Implementación de Coin.


- [Ubicación da Apk](doc/apks)