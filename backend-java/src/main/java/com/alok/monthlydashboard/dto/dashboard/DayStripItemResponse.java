package com.alok.monthlydashboard.dto.dashboard;

import java.time.LocalDate;

public record DayStripItemResponse(
        LocalDate date,
        int dayOfMonth,
        String weekday,
        boolean isToday,
        boolean isWeekend
) {
}