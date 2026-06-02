# Deseño

## Diagrama da arquitectura
```mermaid
graph TD
    UI[Jetpack Compose] --> VM[ViewModel]

    VM --> REPO[Repository]

    REPO -->|solo premium| SUP[Supabase SDK]

    SUP --> AUTH[Gestión de usuarios]
    SUP --> CLOUD[(Base de datos en la nube)]

    VM --> ALARM[AlarmScheduler]
    ALARM -->|AlarmManager| NOTIF[Notificaciones]

    WORK[WorkManager]

    WORK -->|genera intakes diarios| ROOM
    WORK -->|programa alarmas| ALARM

    REPO -->|nuevo medicamento o cambios| ROOM[(Room - SQLite)]
    REPO -->|genera intakes afectados| ALARM
```
## Diagrama de Base de Datos de Room
```mermaid
erDiagram
  USUARIO {
    uuid id PK
    string supabase_uid
    enum tipo_usuario
    string email
    timestamp fecha_premium
    boolean acepta_comunicaciones
    timestamp created_at
    timestamp updated_at
  }

  PERFIL {
    uuid id PK
    uuid usuario_id FK
    string nombre
    string url_imagen
    timestamp created_at
    timestamp updated_at
  }

  MEDICAMENTO {
    uuid id PK
    uuid perfil_id FK
    string nombre
    double dosis
    string unidad
    boolean es_cronico
    date fecha_inicio
    date fecha_fin
    string url_imagen
    timestamp created_at
    timestamp updated_at
  }

  HORARIO {
    uuid id PK
    uuid medicamento_id FK
    int dia_semana
    string hora
  }

  TOMA {
    uuid id PK
    uuid horario_id FK
    timestamp fecha_hora_programada
    timestamp fecha_hora_real
    enum estado
    timestamp created_at
    timestamp updated_at
  }

  USUARIO ||--o{ PERFIL : gestiona
  PERFIL ||--o{ MEDICAMENTO : tiene
  MEDICAMENTO ||--o{ HORARIO : define
  HORARIO ||--o{ TOMA : genera
```

## Diagrama de Base de Datos de Supabase

```mermaid
erDiagram

    AUTH_USERS ||--|| USUARIOS : "user_id"
    AUTH_USERS ||--o{ PROFILES : "user_id"

    PROFILES ||--o{ MEDICATIONS : "profile_id"

    MEDICATIONS ||--o{ SCHEDULES : "medication_id"

    SCHEDULES ||--o{ INTAKES : "schedule_id"

    USUARIOS {
        UUID id PK
        UUID user_id FK
        TEXT tipo_usuario
        TEXT email
        BOOLEAN es_premium
        TIMESTAMPTZ fecha_premium
        BOOLEAN acepta_comunicaciones
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    PROFILES {
        UUID id PK
        UUID user_id FK
        TEXT name
        TEXT image_url
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    MEDICATIONS {
        UUID id PK
        UUID profile_id FK
        TEXT name
        DOUBLE dose
        TEXT unit
        BOOLEAN is_chronic
        DATE start_date
        DATE end_date
        TEXT image_url
        TEXT leaflet_url
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    SCHEDULES {
        UUID id PK
        UUID medication_id FK
        TEXT week_day
        TIME time
    }

    INTAKES {
        UUID id PK
        UUID schedule_id FK
        TIMESTAMPTZ scheduled_at
        TIMESTAMPTZ taken_at
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }
```
## Deseño de interface de usuarios
[Enlace al diseño de Figma](https://www.figma.com/design/7y2djfr8ghHQxxFoeNE2IY/PilRemind?node-id=0-1&t=AqZHzNxyVJfyXzod-1)