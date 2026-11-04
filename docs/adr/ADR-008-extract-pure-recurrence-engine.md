---
title: ADR-008 Extract Pure Recurrence Engine
date: 2026-05-25
product_version: V4
api_version: /api/v1
status: current
---

# ADR-008: Extract Pure Recurrence Engine

## Status

Current

## Date

2026-05-25

## Product Version

V4

## Context

GridSort generates recurring task occurrences for dashboard, checklist, completion, undo, and history workflows.

Before V4, raw recurrence date generation lived inside `RecurrenceServiceImpl` alongside task loading, completion lookup, occurrence status calculation, and response model construction.

The extraction keeps the current task-based application behavior intact.

## Options Considered

1. Keep all recurrence logic in `RecurrenceServiceImpl`.
2. Rewrite the recurrence model around a new Responsibility abstraction.
3. Extract pure recurrence date generation while keeping task orchestration in `RecurrenceServiceImpl`.

## Decision

Extract pure recurrence date generation into `RecurrenceEngine`, while keeping task, completion, and status orchestration inside `RecurrenceServiceImpl`.

## Why

This separates reusable date-generation behavior from current Task-specific application behavior.

`RecurrenceEngine` answers only:

> Given a recurrence rule and a date range, what occurrence dates are generated?

`RecurrenceServiceImpl` remains responsible for adapting current Task entities into recurrence rules and producing existing application responses.

## Consequences

- Recurrence date generation can be tested without Spring, repositories, database entities, DTOs, or UI concepts.
- Existing API behavior remains unchanged.
- Existing database schema remains unchanged.
- Existing frontend behavior remains unchanged.
- Future Responsibility services can reuse the same engine.

## Trade-offs

- Adds another backend layer.
- Requires mapping from current Task persistence fields into `RecurrenceRule`.
- Does not remove the existing split between common enums and entity enums.

## Revisit When

- A Responsibility model is added beside Task.
- New recurrence types are introduced.
- API v1 needs new recurrence capabilities.
- The adapter mapping in `RecurrenceServiceImpl` becomes too large.

## Codebase Evidence

- `backend-java/src/main/java/com/alok/monthlydashboard/recurrence/RecurrenceEngine.java`
- `backend-java/src/main/java/com/alok/monthlydashboard/recurrence/RecurrenceRule.java`
- `backend-java/src/main/java/com/alok/monthlydashboard/service/impl/RecurrenceServiceImpl.java`
- `backend-java/src/test/java/com/alok/monthlydashboard/recurrence/RecurrenceEngineTest.java`
- `backend-java/src/test/java/com/alok/monthlydashboard/service/RecurrenceServiceCriticalTest.java`
- `backend-java/src/test/java/com/alok/monthlydashboard/service/ChecklistIntegrationTest.java`
- `backend-java/src/test/java/com/alok/monthlydashboard/service/DashboardTimelineIntegrationTest.java`

## Related Docs

- [V4 Recurrence Architecture](../22-v4-recurrence-architecture.md)
- [GridSort README](../../README.md)
