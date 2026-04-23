package com.alok.monthlydashboard.dto.dashboard;

import java.time.LocalDate;
import java.util.List;

public record MonthlyDashboardResponse(
        int year,
        int month,
        String monthLabel,
        LocalDate today,
        boolean readOnly,
        ScaleBarResponse scaleBar,
        List<DayStripItemResponse> dayStrip,
        List<DashboardCategoryResponse> categories
) {
}