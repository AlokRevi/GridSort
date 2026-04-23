package com.alok.monthlydashboard.dto.history;

import com.alok.monthlydashboard.dto.checklist.OccurrenceStatus;

import java.time.LocalDate;

public record TaskHistoryItemResponse(
        LocalDate occurrenceDate,
        LocalDate completionDate,
        OccurrenceStatus status
) {
}