# Monthly Dashboard

Monthly Dashboard is a Spring Boot and Angular app for managing recurring responsibilities as generated task occurrences. The monthly grid gives a read-only overview, while the Today Checklist is the main action surface for overdue and due-today work.

## What It Does

- Creates categories and recurring tasks.
- Generates monthly occurrences dynamically from recurrence rules.
- Tracks completion history by `occurrenceDate`.
- Shows overdue, due-today, upcoming, and completed occurrences.
- Groups the Today Checklist by category requirement.
- Exports current system data as JSON for backup.
- Supports a focused mobile view for checklist and task entry.

## Current V2 Features

- Backend-controlled current date provider for testable date logic.
- JSON export endpoint at `GET /api/v1/export`.
- Recurrence edge-case coverage for leap years, fallbacks, boundaries, and weekday rules.
- Dashboard UI split into focused Angular components with page state handled by a state service.
- Softer monthly grid colors with progressive past-day styling.
- Category metadata:
  - `requires`: `FOCUS`, `MOVEMENT`, `OUTDOOR`
  - category profile values carried through the current API model
- Category creation through a dialog.
- Backend-generated recurrence summaries for category/task displays.
- Today Checklist grouped by category `requires`.
- Interval recurrence supports `DAYS`, `WEEKS`, and `MONTHS`.
- Safe scoped task editing for future changes.

## Recurrence Support

Current recurrence types:

- `FIXED_DATE`: one or more days of month, with optional fallback to the last valid day.
- `INTERVAL`: every N days, weeks, or months.
- `WEEKDAY`: first, second, third, fourth, or last weekday of a month.

Monthly interval behavior preserves the original start-date day when possible. If the target month does not contain that day, it falls back to the last valid day of that month.

## Scoped Editing

Implemented edit scopes:

- `THIS_AND_FOLLOWING`
- `ALL_FUTURE`

Both scopes use a safe split model:

- the existing task ends before the selected occurrence
- a new successor task starts at the selected occurrence
- existing completion history stays attached to the original task

Not implemented:

- true `THIS_OCCURRENCE`
- per-occurrence overrides
- rewriting historical recurrence meaning

## Mobile Behavior

Phone-sized screens show the action-focused flow:

- Today Checklist
- Add Task
- Create Category dialog entry point

The monthly grid, timeline, and categories management panel remain available on larger screens.

## Project Paths

- Backend API: `backend-java`
- Frontend app: `frontend-angular/frontend-angular`
- Archived prototypes: `archive/frontend-prototypes`

New frontend work should happen in `frontend-angular/frontend-angular`.

## Run The Backend

```bash
cd backend-java
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8080
```

Default local SQLite database:

```text
backend-java/monthly-dashboard.db
```

## Run The Frontend

```bash
cd frontend-angular/frontend-angular
npm install
npm start
```

Frontend URL:

```text
http://localhost:4200
```

## Configuration

Angular API URLs:

```text
frontend-angular/frontend-angular/src/environments/environment.ts
frontend-angular/frontend-angular/src/environments/environment.prod.ts
```

Backend CORS configuration:

```text
backend-java/src/main/resources/application.properties
```

Deployment example:

```powershell
$env:APP_CORS_ALLOWED_ORIGINS="https://your-domain.com"
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

## Current Limitations

- The app enforces a maximum of 15 active tasks.
- Import is not implemented yet.
- True single-occurrence editing is deferred.
- Task-level profile overrides are deferred.
- The monthly grid is intentionally read-only; checklist actions are the primary completion flow.
