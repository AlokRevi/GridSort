# Deployment Checklist

This checklist targets a private V2 deployment of Monthly Dashboard.

## Required Environment Variables

Backend production profile:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:APP_DATASOURCE_URL="jdbc:sqlite:C:/path/to/monthly-dashboard.db"
$env:APP_CORS_ALLOWED_ORIGINS="https://your-dashboard-domain.example"
```

Optional backend settings:

```powershell
$env:APP_JPA_DDL_AUTO="update"
$env:APP_LOG_LEVEL="INFO"
```

For a stable existing database, prefer:

```powershell
$env:APP_JPA_DDL_AUTO="validate"
```

## Backend Build And Startup

Build:

```bash
cd backend-java
mvn test
mvn package
```

Run:

```bash
java -jar target/monthly-dashboard-0.0.1-SNAPSHOT.jar
```

For local development:

```bash
cd backend-java
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Frontend Build And Deploy

Build:

```bash
cd frontend-angular/frontend-angular
npm install
npm run build
```

Deploy the contents of:

```text
frontend-angular/frontend-angular/dist/frontend-angular
```

The production frontend currently uses:

```text
/api/v1
```

This assumes the frontend and backend are served from the same origin, or that the host/reverse proxy forwards `/api/v1` to the backend.

## SQLite Backup

- Stop the backend before copying the database file, or use SQLite's online backup tooling.
- Back up the main `.db` file and any active `-wal`/`-shm` files if the app is running in WAL mode.
- Store backups outside the deployment directory.
- Test restore before treating backups as reliable.

## Post-Deploy Smoke Tests

- Load the frontend and confirm the dashboard renders.
- Confirm `GET /api/v1/checklist/today` returns checklist items with `categoryRequires` and `categoryColor`.
- Confirm Today Checklist grouping renders.
- Mark a due/overdue item complete, then undo from the checkmark.
- Open category edit from the monthly occurrence section.
- Open category edit from the right-side Categories panel.
- Open task edit and save with task profile overrides set to category default.
- Confirm `GET /api/v1/export` returns categories, tasks, recurrence rules, and completion history.

## Current Private V2 Notes

- The app has no authentication yet. Keep the deployment private behind trusted access controls.
- Production CORS should be restricted to the deployed frontend origin.
- The monthly grid is read-only; completion actions happen through the Today Checklist.
- True `THIS_OCCURRENCE` and per-occurrence task overrides are not implemented.
