package com.alok.monthlydashboard.dto.dashboard;

import java.util.List;

public record DashboardCategoryResponse(
        Long categoryId,
        String categoryName,
        List<DashboardTaskResponse> tasks
) {
}