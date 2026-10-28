# Deseño

## Diagrama da arquitectura

```mermaid
graph TD
    UI[Jetpack Compose] --> VM[ViewModel]

    VM --> REPO[Repository]

    REPO --> ROOM[(Room - SQLite)]
    REPO -->|programa alarmas| ALARM[AlarmScheduler]

    ALARM -->|AlarmManager| NOTIF[Notificacións]

    WORK[WorkManager]
    WORK -->|mantén a ventá de 7 días| ROOM
    WORK -->|programa alarmas| ALARM
```

> A versión con Supabase consérvase unicamente nas ramas históricas. A rama actual é local-only: todos os datos se gardan en Room no dispositivo.

## Diagrama de Base de Datos de Room

```mermaid
erDiagram
  USUARIO {
    int id PK
    timestamp createdAt
    timestamp updatedAt
  }

  PERFIL {
    int id PK
    int usuarioId FK
    string name
    string imageUrl
    timestamp createdAt
    timestamp updatedAt
  }

  MEDICAMENTO {
    int id PK
    int profileId FK
    string name
    double dose
    string unit
    boolean isChronic
    date startDate
    date endDate
    string imageUrl
    string leafletUrl
    timestamp createdAt
    timestamp updatedAt
    timestamp deletedAt
  }

  HORARIO {
    int id PK
    int medicationId FK
    string weekDay
    time time
  }

  TOMA {
    int id PK
    int medicationId FK
    int scheduleId FK "opcional; SET NULL ao borrar o horario"
    timestamp scheduledAt
    timestamp takenAt
    string medicationNameSnapshot
    double doseSnapshot
    string unitSnapshot
    timestamp createdAt
    timestamp updatedAt
  }

  USUARIO ||--o{ PERFIL : xestiona
  PERFIL ||--o{ MEDICAMENTO : ten
  MEDICAMENTO ||--o{ HORARIO : define
  MEDICAMENTO ||--o{ TOMA : rexistra
  HORARIO ||--o{ TOMA : xera
```

- Un medicamento pode ter varios horarios e varias tomas.
- Un horario pode xerar varias tomas ao longo do tempo.
- Cada toma pertence a un medicamento e pode apuntar a un horario ou quedar sen el.
- `medicationNameSnapshot`, `doseSnapshot` e `unitSnapshot` conservan os datos do medicamento no momento de crear a toma.
- `deletedAt` permite arquivar un medicamento sen borrar o seu historial.
- As columnas de clave foránea (`usuarioId`, `profileId`, `medicationId`) teñen índices para acelerar as relacións e os borrados en cascada.

## Deseño de interface de usuarios
[Enlace al diseño de Figma](https://www.figma.com/design/7y2djfr8ghHQxxFoeNE2IY/PilRemind?node-id=0-1&t=AqZHzNxyVJfyXzod-1)
