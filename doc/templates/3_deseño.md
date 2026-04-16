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
    string estado
    timestamp created_at
    timestamp updated_at
  }

  USUARIO ||--o{ PERFIL : gestiona
  PERFIL ||--o{ MEDICAMENTO : tiene
  MEDICAMENTO ||--o{ HORARIO : define
  HORARIO ||--o{ TOMA : genera
```
## Deseño de interface de usuarios
[Enlace al diseño de Figma](https://www.figma.com/design/7y2djfr8ghHQxxFoeNE2IY/PilRemind?node-id=0-1&t=AqZHzNxyVJfyXzod-1)