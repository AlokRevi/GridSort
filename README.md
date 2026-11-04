# GridSort

GridSort is a private, single-user app for managing recurring responsibilities. It generates task occurrences from recurrence rules, offers read-only planning timelines from a month to a year, and uses the Today Checklist as the main place to complete work.

Monthly Dashboard is the original/internal project name, still used in repository paths and parts of the UI. GridSort is the product name.

## Current Shape

- Backend: Spring Boot API in `backend-java`
- Frontend: Angular app in `frontend-angular/frontend-angular`
- Database: SQLite
- Java version: 17

## Core Concepts

- Categories group tasks and provide requirement and feel-label metadata.
- Tasks define recurrence rules.
- Occurrences are generated dynamically; they are not stored as separate rows.
- Completion history is stored against `occurrenceDate`.
- Planning grids are intentionally read-only.
- The Today Checklist is the primary action surface.

## Current Features

- Read-only Month, Quarter (3 months), Quadrimester (4 months), HalfYear (6 months), and Year timelines with generated occurrences.
- Month uses daily cells; Quarter and Quadrimester group occurrences into weekday/weekend buckets; HalfYear and Year use weekly buckets.
- Today Checklist for due-today work and incomplete overdue occurrences from the current and previous month. Completed occurrences due today remain visible for undo; completed overdue occurrences leave the list.
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
- Category metadata:
  - `requires`
  - `feelsLike` labels
- Optional task-level profile overrides in the Edit Task modal.
- Backend-generated recurrence summaries.
- Full JSON export, including completion history:

```text
GET /api/v1/export
```

- Setup export and previewed import for moving categories and tasks to an empty installation (see below).

- Mobile action view focused on:
  - Today Checklist
  - Add Task
  - Create Category access

## Today Checklist

The checklist is the main place to complete and undo work; timeline grids provide planning context. Only active tasks contribute checklist items. The backend determines today using its local system date, independently of the selected planning month. Future occurrences cannot be completed.

Completion history records both the scheduled `occurrenceDate` and the actual `completionDate`, so completing overdue work preserves its original due date.

## Setup Import And Export

Use **Export Setup** to download a `v2-setup-snapshot` JSON file, then **Import Setup** to select it, review the preview counts, warnings, and validation errors, and confirm the import.

Setup import requires an empty installation: there must be no categories and no tasks, including inactive tasks. The only supported mode is `EMPTY_ONLY`; import does not merge with or replace an existing setup. The preview validates the file; the empty-installation check happens when import is confirmed.

Setup snapshots contain categories, tasks, recurrence rules, and profile metadata. Setup export excludes completion history, and setup import does not restore it. Full export includes completion history but is a separate format, with no corresponding full-restore import endpoint.

```text
GET  /api/v1/export/setup
POST /api/v1/import/setup/preview
POST /api/v1/import/setup?mode=EMPTY_ONLY
```

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

Categories store a `requires` value and a list of `feelsLike` labels. These appear in checklist grouping and metadata.

Optional task override fields:

- `energyOverride`
- `enjoymentOverride`
- `pressureOverride`
- `effortOverride`

Task overrides are stored and editable, but do not affect recurrence or completion logic. Categories no longer store four separate energy, enjoyment, pressure, and effort defaults.

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

## Architecture

Recurrence date generation is handled by the pure backend `RecurrenceEngine`. The service layer loads tasks, applies completion status, and builds dashboard, checklist, and history responses.

See the [recurrence architecture notes](docs/22-v4-recurrence-architecture.md) and [recurrence-engine decision record](docs/adr/ADR-008-extract-pure-recurrence-engine.md).

## Project Paths

```text
backend-java/                         Spring Boot backend
frontend-angular/frontend-angular/    Angular frontend
archive/frontend-prototypes/          Archived prototypes
```

New frontend work should happen in:

```text
frontend-angular/frontend-angular
```

## Current Limitations

- Private/single-user deployment only.
- No app-level authentication yet.
- Task creation and activation enforce a limit of 15 active tasks; setup import currently does not enforce that cap.
- Setup import supports empty installations only and excludes completion history.
- True single-occurrence editing is deferred.
- Per-occurrence overrides are not implemented.
- Timeline grids are read-only by design.
- Today Checklist only considers occurrences from the current and previous month.
- Task profile overrides do not affect recurrence or completion logic.
