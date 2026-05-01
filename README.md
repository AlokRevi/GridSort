# Monthly Dashboard

Monthly Dashboard is a private, single-user app for managing recurring responsibilities. It generates task occurrences from recurrence rules, shows the month as a read-only planning view, and uses the Today Checklist as the main place to complete work.

The current codebase is the stable private V2 checkpoint.

## Current Shape

- Backend: Spring Boot API in `backend-java`
- Frontend: Angular app in `frontend-angular/frontend-angular`
- Database: SQLite
- Deployment target: private VPS/server with Tailscale + Caddy
- Java version: 17

## Core Concepts

- Categories group tasks and provide default profile metadata.
- Tasks define recurrence rules.
- Occurrences are generated dynamically; they are not stored as separate rows.
- Completion history is stored against `occurrenceDate`.
- The monthly grid is intentionally read-only.
- The Today Checklist is the primary action surface.

## Current V2 Features

- Read-only monthly dashboard with generated occurrences.
- Today Checklist for overdue and due-today work.
- Checklist grouping by category requirement:
  - `FOCUS`
  - `MOVEMENT`
  - `OUTDOOR`
- Completion and undo completion from the checklist.
- Category create/edit dialog.
- Category editing from:
  - Add Task flow
  - monthly occurrence section
  - right-side Categories panel
- Structured category profile defaults:
  - `requires`
  - `energy`
  - `enjoyment`
  - `pressure`
  - `effort`
- Optional task-level profile overrides in the Edit Task modal.
- Backend-generated recurrence summaries.
- JSON export endpoint for backup:

```text
GET /api/v1/export
```

- Mobile action view focused on:
  - Today Checklist
  - Add Task
  - Create Category access

## Recurrence Support

Supported recurrence types:

- `FIXED_DATE`: one or more days of the month, with optional fallback to the last valid day.
- `INTERVAL`: every N days, weeks, or months.
- `WEEKDAY`: first, second, third, fourth, or last weekday of a month.

Monthly interval behavior preserves the original anchor day when possible. If the target month does not contain that day, it falls back to the last valid day of that month.

Examples:

```text
Jan 31 + 1 month -> Feb 28 or Feb 29
Aug 31 + 1 month -> Sep 30
```

## Scoped Task Editing

Implemented scopes:

- `THIS_AND_FOLLOWING`
- `ALL_FUTURE`

Both use a safe split model:

- the existing task ends before the selected occurrence
- a new successor task starts at the selected occurrence
- old completion history remains attached to the original task

Not implemented:

- true `THIS_OCCURRENCE`
- per-occurrence overrides
- rewriting historical recurrence meaning

## Category And Task Profiles

Category profile values are defaults for tasks in that category.

Profile fields:

- `requires`
- `energy`
- `enjoyment`
- `pressure`
- `effort`

Task profile fields are optional overrides. A `null` task override means "use category default".

Profile values:

- Energy: `DEATHLY_DRAINING`, `TIRING`, `ACTIVATING`, `ENERGIZING`
- Enjoyment: `BORING`, `OKAY`, `FUN`, `BLISSFUL`
- Pressure: `NO_PRESSURE`, `MILD_FUTURE_STRESS`, `URGENT_AND_IMPORTANT`, `AMORPHOUS_DREAD`
- Effort: `EASY`, `MEDIUM`, `HARD`, `VERY_HARD`

## Local Development

### Prerequisites

- Java 17
- Maven
- Node/npm

### Run Backend

```bash
cd backend-java
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend URL:

```text
http://localhost:8080
```

Default local SQLite database:

```text
backend-java/monthly-dashboard.db
```

### Run Frontend

```bash
cd frontend-angular/frontend-angular
npm install
npm start
```

Frontend URL:

```text
http://localhost:4200
```

## Verify

Backend:

```bash
cd backend-java
mvn test
```

Frontend:

```bash
cd frontend-angular/frontend-angular
npm run build
```

## Configuration

Backend config:

```text
backend-java/src/main/resources/application.properties
backend-java/src/main/resources/application-dev.properties
backend-java/src/main/resources/application-prod.properties
```

Frontend environment config:

```text
frontend-angular/frontend-angular/src/environments/environment.ts
frontend-angular/frontend-angular/src/environments/environment.prod.ts
```

Production frontend API base:

```text
/api/v1
```

That expects same-origin routing or a reverse proxy that sends `/api/v1` to the backend.

## Deployment

The selected private V2 deployment path is:

- small private VPS/server
- Tailscale for private access
- Caddy for same-origin reverse proxy
- Angular static frontend
- Spring Boot backend on `127.0.0.1:8080`
- SQLite database on persistent disk

Use [DEPLOYMENT.md](DEPLOYMENT.md) for the exact first-deploy runbook.

## Project Paths

```text
backend-java/                         Spring Boot backend
frontend-angular/frontend-angular/    Angular frontend
archive/frontend-prototypes/          Archived prototypes
DEPLOYMENT.md                         Private VPS deployment runbook
```

New frontend work should happen in:

```text
frontend-angular/frontend-angular
```

## Current Limitations

- Private/single-user deployment only.
- No app-level authentication yet.
- Maximum of 15 active tasks.
- Import is not implemented.
- True single-occurrence editing is deferred.
- Per-occurrence overrides are not implemented.
- Monthly grid is read-only by design.
- Task profile overrides do not affect recurrence or completion logic.
