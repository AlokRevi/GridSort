package com.alok.monthlydashboard.dto.checklist;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CompleteTaskRequest(
        @NotNull
        LocalDate occurrenceDate,

        @NotNull
        LocalDate completionDate
) {
}