
# Codificación y Pruebas

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


