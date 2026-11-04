---
title: GridSort Recurrence Architecture
date: 2026-05-25
product_version: V4
api_version: /api/v1
status: current
---

# GridSort Recurrence Architecture

## Summary

V4 extracts raw recurrence date generation from the task-aware service layer into a pure backend recurrence package.

GridSort retains its current task-based behavior. V4 does not introduce Responsibility CRUD, does not migrate Task into Responsibility, does not change API routes, and does not change the frontend.

## What V4 Changed

- Added `com.alok.monthlydashboard.recurrence.RecurrenceEngine`.
- Added `com.alok.monthlydashboard.recurrence.RecurrenceRule`.
- Moved raw date-generation logic out of `RecurrenceServiceImpl`.
- Kept `RecurrenceServiceImpl` as the adapter between Task persistence, completions, occurrence statuses, and generated dates.
- Added pure recurrence engine tests.
- Added characterization and integration test coverage before extraction.

## What Stayed Unchanged

- Current API version remains `/api/v1`.
- Existing API response shapes remain unchanged.
- Existing controllers remain unchanged.
- Existing DTO response models remain unchanged.
- Existing database schema remains unchanged.
- Existing frontend behavior remains unchanged.
- Task remains the current persisted responsibility-like model.
- Completion and undo behavior remain in the service layer.
- Occurrence status calculation remains in `RecurrenceServiceImpl`.

## Why Recurrence Was Extracted

Recurring date generation is reusable domain logic. Before V4, date generation was mixed into `RecurrenceServiceImpl`, which also knew about tasks, repositories, completions, statuses, and API response models.

V4 separates the pure question:

> Given a recurrence rule and a date range, what occurrence dates are generated?

from task-specific orchestration:

> Given a task, completions, today's date, and a dashboard/checklist/history request, what response should the app return?

## Architecture Flow

```text
Dashboard / Checklist / History
    |
    v
RecurrenceServiceImpl
    |
    v
RecurrenceEngine
```

`DashboardService`, `ChecklistService`, and history-facing flows continue to call the existing recurrence service contract. They do not call the engine directly.

## Pure RecurrenceEngine

`RecurrenceEngine` is pure date-generation logic. It does not depend on:

- `Task`
- `TaskRepository`
- `TaskCompletionRepository`
- DTOs
- `OccurrenceResponse`
- `OccurrenceStatus`
- Spring annotations
- `DashboardService`
- `ChecklistService`
- History services
- `AppDateProvider`
- Angular or UI concepts

Its public responsibility is to generate occurrence dates for a `RecurrenceRule` over a requested date range.

## Logic Moved Into RecurrenceEngine

The engine owns date generation for:

- fixed-date recurrence
- multiple fixed dates
- fallback to the last valid day of a short month
- disabled fallback for impossible dates
- duplicate fallback date collapse
- interval recurrence
- daily and weekly interval cadence across month boundaries
- monthly interval anchoring from the task start date, including Jan 31 behavior
- weekday recurrence
- start date and optional end date boundaries
- inactive rule exclusion
- inverted date range returning no dates

## Logic Remaining In RecurrenceServiceImpl

`RecurrenceServiceImpl` remains the task-aware adapter. It still owns:

- loading tasks by ID
- mapping `Task` and `TaskRecurrenceRule` into `RecurrenceRule`
- validating missing task recurrence rule records where current behavior requires it
- querying task completions
- calculating `OccurrenceStatus`
- building `OccurrenceResponse`
- preserving existing public recurrence service methods
- preserving existing behavior for dashboard, checklist, completion, undo, and history flows

## Test Coverage

V4 is protected by:

- `RecurrenceServiceCriticalTest`
- `RecurrenceEngineTest`
- `ChecklistIntegrationTest`
- `DashboardTimelineIntegrationTest`

Coverage includes:

- fixed-date recurrence
- multiple fixed dates
- 31st fallback enabled
- 31st fallback disabled
- interval recurrence across month boundaries
- weekly interval recurrence
- monthly interval from Jan 31
- weekday recurrence
- weekday start/end date boundaries
- duplicate fallback dates collapsed
- dashboard monthly generation
- today checklist behavior
- completion and undo behavior
- history behavior

Run the backend checks locally with `mvn test` from `backend-java`.

## Known Risks

- The app still has both common recurrence enums and entity recurrence enums. V4 did not remove or redesign that split.
- `RecurrenceServiceImpl` still performs task-to-rule mapping. This is intentional, but it is now the main adapter boundary to watch.
- `RecurrenceEngine` is currently backend-only. No frontend surface exposes it directly.
- Older V3 architecture docs may describe the pre-extraction recurrence service layout.

## Related Documentation

- [GridSort README](../README.md)
- [Recurrence-engine decision record](adr/ADR-008-extract-pure-recurrence-engine.md)
