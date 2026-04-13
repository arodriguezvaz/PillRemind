# Deseño

## Diagrama da arquitectura
```mermaid
graph TD
    UI[Jetpack Compose] --> VM[ViewModel + Coroutines]
    VM --> REPO[Repository]
    REPO --> ROOM[(Room - SQLite local)]
    REPO -->|solo premium| SUP[Supabase SDK]
    SUP --> AUTH[Gestión de usuarios]
    SUP --> STORAGE[(Base de datos en la nube)]
    ALARM[AlarmScheduler] -->|AlarmManager| NOTIF[Notificaciones]
    VM --> ALARM
    WORK[WorkManager] -->|sync background| SUP
    REPO -->|premium| WORK
```
## Diagrama de Base de Datos
```mermaid
erDiagram
  USUARIO {
    uuid id PK
    string email
    boolean es_premium
    timestamp fecha_premium
    boolean acepta_comunicaciones
  }
  PERFIL {
    uuid id PK
    uuid usuario_id FK
    string nombre
    string url_imagen
  }
  MEDICAMENTO {
    long id PK
    uuid perfil_id FK
    string nombre
    double dosis
    string unidad
    boolean es_cronico
    date fecha_inicio
    date fecha_fin
    string dias_semana
    string horas
    string url_imagen
  }
  TOMA {
    uuid id PK
    long medicamento_id FK
    timestamp fecha_hora_programada
    timestamp fecha_hora_real
    boolean administrada
  }

  USUARIO ||--o{ PERFIL : gestiona
  PERFIL ||--o{ MEDICAMENTO : tiene
  MEDICAMENTO ||--o{ TOMA : genera
```
## Deseño de interface de usuarios
[Enlace al diseño de Figma](https://www.figma.com/design/7y2djfr8ghHQxxFoeNE2IY/PilRemind?node-id=0-1&t=AqZHzNxyVJfyXzod-1)