package com.alok.monthlydashboard.dto.checklist;

import java.time.LocalDate;
import java.util.List;

public record TodayChecklistResponse(
        LocalDate today,
        List<ChecklistItemResponse> items
) {
}