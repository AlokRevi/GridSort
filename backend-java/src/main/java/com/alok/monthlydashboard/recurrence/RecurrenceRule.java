package com.alok.monthlydashboard.recurrence;

import com.alok.monthlydashboard.common.enums.IntervalUnit;
import com.alok.monthlydashboard.common.enums.RecurrenceType;
import com.alok.monthlydashboard.common.enums.WeekOfMonth;
import com.alok.monthlydashboard.common.enums.Weekday;

import java.time.LocalDate;
import java.util.List;

public record RecurrenceRule(
        RecurrenceType recurrenceType,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        List<Integer> fixedDates,
        Boolean fallbackToLastDay,
        Integer intervalValue,
        IntervalUnit intervalUnit,
        Weekday weekday,
        WeekOfMonth weekOfMonth
) {
    public RecurrenceRule {
        fixedDates = fixedDates == null ? List.of() : List.copyOf(fixedDates);
    }
}
