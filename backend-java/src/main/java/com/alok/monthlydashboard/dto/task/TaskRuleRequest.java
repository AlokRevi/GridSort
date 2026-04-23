package com.alok.monthlydashboard.dto.task;

import java.util.List;

public record TaskRuleRequest(
        List<Integer> fixedDates,
        Integer intervalValue,
        IntervalUnit intervalUnit,
        Weekday weekday,
        WeekOfMonth weekOfMonth,
        Boolean fallbackToLastDay
) {
}