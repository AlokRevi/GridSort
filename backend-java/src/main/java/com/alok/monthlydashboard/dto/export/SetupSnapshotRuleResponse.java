package com.alok.monthlydashboard.dto.export;

import com.alok.monthlydashboard.entity.enums.IntervalUnit;
import com.alok.monthlydashboard.entity.enums.WeekOfMonth;
import com.alok.monthlydashboard.entity.enums.Weekday;

import java.util.List;

public record SetupSnapshotRuleResponse(
        List<Integer> fixedDates,
        Boolean fallbackToLastDay,
        Integer intervalValue,
        IntervalUnit intervalUnit,
        Weekday weekday,
        WeekOfMonth weekOfMonth
) {
}
