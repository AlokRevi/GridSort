package com.alok.monthlydashboard.dto.task;

import java.time.LocalDate;

public record TaskDetailResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        RecurrenceType recurrenceType,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive,
        TaskRuleRequest rule
) {
}