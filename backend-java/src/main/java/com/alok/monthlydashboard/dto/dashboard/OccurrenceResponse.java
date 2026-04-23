package com.alok.monthlydashboard.dto.dashboard;

import com.alok.monthlydashboard.dto.checklist.OccurrenceStatus;

import java.time.LocalDate;

public record OccurrenceResponse(
        LocalDate occurrenceDate,
        int dayOfMonth,
        boolean completed,
        LocalDate completionDate,
        OccurrenceStatus status
) {
}