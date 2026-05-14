package com.alok.monthlydashboard.dto.export;

import com.alok.monthlydashboard.common.enums.FeelsLikeLabel;
import com.alok.monthlydashboard.common.enums.RecurrenceType;

import java.time.LocalDate;

public record SetupSnapshotTaskResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        String description,
        RecurrenceType recurrenceType,
        LocalDate startDate,
        LocalDate endDate,
        boolean isActive,
        FeelsLikeLabel energyOverride,
        FeelsLikeLabel enjoymentOverride,
        FeelsLikeLabel pressureOverride,
        FeelsLikeLabel effortOverride,
        SetupSnapshotRuleResponse rule
) {
}
