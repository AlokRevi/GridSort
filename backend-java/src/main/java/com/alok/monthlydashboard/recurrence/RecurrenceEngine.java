package com.alok.monthlydashboard.recurrence;

import com.alok.monthlydashboard.common.enums.IntervalUnit;
import com.alok.monthlydashboard.common.enums.WeekOfMonth;
import com.alok.monthlydashboard.common.enums.Weekday;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RecurrenceEngine {

    public List<LocalDate> generateOccurrenceDatesBetween(
            RecurrenceRule rule,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (rule == null || startDate.isAfter(endDate)) {
            return List.of();
        }

        List<LocalDate> dates = new ArrayList<>();
        YearMonth cursor = YearMonth.from(startDate);
        YearMonth finalMonth = YearMonth.from(endDate);

        while (!cursor.isAfter(finalMonth)) {
            dates.addAll(generateOccurrenceDates(rule, cursor)
                    .stream()
                    .filter(date -> !date.isBefore(startDate) && !date.isAfter(endDate))
                    .toList());
            cursor = cursor.plusMonths(1);
        }

        dates.sort(LocalDate::compareTo);
        return dates;
    }

    private List<LocalDate> generateOccurrenceDates(RecurrenceRule rule, YearMonth targetMonth) {
        if (rule == null || !rule.active()) {
            return List.of();
        }

        return switch (rule.recurrenceType()) {
            case FIXED_DATE -> generateFixedDateOccurrences(rule, targetMonth);
            case INTERVAL -> generateIntervalOccurrences(rule, targetMonth);
            case WEEKDAY -> generateWeekdayOccurrences(rule, targetMonth);
        };
    }

    private List<LocalDate> generateFixedDateOccurrences(RecurrenceRule rule, YearMonth targetMonth) {
        List<LocalDate> dates = new ArrayList<>();

        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        if (monthEnd.isBefore(rule.startDate())) {
            return dates;
        }
        if (rule.endDate() != null && monthStart.isAfter(rule.endDate())) {
            return dates;
        }

        boolean fallbackToLastDay = rule.fallbackToLastDay() == null || rule.fallbackToLastDay();

        for (Integer fixedDate : rule.fixedDates()) {
            if (fixedDate == null) {
                continue;
            }

            int actualDay = fixedDate;

            if (fixedDate > targetMonth.lengthOfMonth()) {
                if (fallbackToLastDay) {
                    actualDay = targetMonth.lengthOfMonth();
                } else {
                    continue;
                }
            }

            LocalDate occurrenceDate = targetMonth.atDay(actualDay);

            if (occurrenceDate.isBefore(rule.startDate())) {
                continue;
            }
            if (rule.endDate() != null && occurrenceDate.isAfter(rule.endDate())) {
                continue;
            }

            if (!dates.contains(occurrenceDate)) {
                dates.add(occurrenceDate);
            }
        }

        dates.sort(LocalDate::compareTo);
        return dates;
    }

    private List<LocalDate> generateIntervalOccurrences(RecurrenceRule rule, YearMonth targetMonth) {
        List<LocalDate> dates = new ArrayList<>();

        if (rule.intervalValue() == null || rule.intervalValue() <= 0) {
            return dates;
        }
        if (rule.intervalUnit() == null) {
            return dates;
        }

        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        if (monthEnd.isBefore(rule.startDate())) {
            return dates;
        }
        if (rule.endDate() != null && monthStart.isAfter(rule.endDate())) {
            return dates;
        }

        if (rule.intervalUnit() == IntervalUnit.MONTHS) {
            return generateMonthlyIntervalOccurrences(rule, targetMonth);
        }

        LocalDate cursor = rule.startDate();

        while (cursor.isBefore(monthStart)) {
            cursor = advanceInterval(cursor, rule.intervalValue(), rule.intervalUnit());
        }

        while (!cursor.isAfter(monthEnd)) {
            if (!cursor.isBefore(rule.startDate())
                    && (rule.endDate() == null || !cursor.isAfter(rule.endDate()))) {
                dates.add(cursor);
            }
            cursor = advanceInterval(cursor, rule.intervalValue(), rule.intervalUnit());
        }

        return dates;
    }

    private List<LocalDate> generateMonthlyIntervalOccurrences(RecurrenceRule rule, YearMonth targetMonth) {
        List<LocalDate> dates = new ArrayList<>();

        YearMonth startMonth = YearMonth.from(rule.startDate());
        long monthsFromStart = ChronoUnit.MONTHS.between(startMonth, targetMonth);

        if (monthsFromStart < 0 || monthsFromStart % rule.intervalValue() != 0) {
            return dates;
        }

        LocalDate occurrenceDate = anchoredMonthlyDate(targetMonth, rule.startDate().getDayOfMonth());

        if (occurrenceDate.isBefore(rule.startDate())) {
            return dates;
        }
        if (rule.endDate() != null && occurrenceDate.isAfter(rule.endDate())) {
            return dates;
        }

        dates.add(occurrenceDate);
        return dates;
    }

    private LocalDate anchoredMonthlyDate(YearMonth targetMonth, int anchorDay) {
        return targetMonth.atDay(Math.min(anchorDay, targetMonth.lengthOfMonth()));
    }

    private List<LocalDate> generateWeekdayOccurrences(RecurrenceRule rule, YearMonth targetMonth) {
        List<LocalDate> dates = new ArrayList<>();

        if (rule.weekday() == null || rule.weekOfMonth() == null) {
            return dates;
        }

        LocalDate monthStart = targetMonth.atDay(1);
        LocalDate monthEnd = targetMonth.atEndOfMonth();

        if (monthEnd.isBefore(rule.startDate())) {
            return dates;
        }
        if (rule.endDate() != null && monthStart.isAfter(rule.endDate())) {
            return dates;
        }

        DayOfWeek targetDayOfWeek = mapWeekday(rule.weekday());
        List<LocalDate> matchingDays = new ArrayList<>();

        LocalDate cursor = monthStart;
        while (!cursor.isAfter(monthEnd)) {
            if (cursor.getDayOfWeek() == targetDayOfWeek) {
                matchingDays.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }

        LocalDate occurrenceDate = selectWeekdayOccurrence(matchingDays, rule.weekOfMonth());

        if (occurrenceDate == null) {
            return dates;
        }

        if (occurrenceDate.isBefore(rule.startDate())) {
            return dates;
        }
        if (rule.endDate() != null && occurrenceDate.isAfter(rule.endDate())) {
            return dates;
        }

        dates.add(occurrenceDate);
        return dates;
    }

    private LocalDate advanceInterval(LocalDate date, int intervalValue, IntervalUnit unit) {
        return switch (unit) {
            case DAYS -> date.plusDays(intervalValue);
            case WEEKS -> date.plusWeeks(intervalValue);
            case MONTHS -> throw new IllegalArgumentException("Monthly intervals require start-date anchored generation");
        };
    }

    private DayOfWeek mapWeekday(Weekday weekday) {
        return switch (weekday) {
            case MONDAY -> DayOfWeek.MONDAY;
            case TUESDAY -> DayOfWeek.TUESDAY;
            case WEDNESDAY -> DayOfWeek.WEDNESDAY;
            case THURSDAY -> DayOfWeek.THURSDAY;
            case FRIDAY -> DayOfWeek.FRIDAY;
            case SATURDAY -> DayOfWeek.SATURDAY;
            case SUNDAY -> DayOfWeek.SUNDAY;
        };
    }

    private LocalDate selectWeekdayOccurrence(List<LocalDate> matchingDays, WeekOfMonth weekOfMonth) {
        if (matchingDays.isEmpty()) {
            return null;
        }

        return switch (weekOfMonth) {
            case FIRST -> getByIndexOrLast(matchingDays, 0);
            case SECOND -> getByIndexOrLast(matchingDays, 1);
            case THIRD -> getByIndexOrLast(matchingDays, 2);
            case FOURTH -> getByIndexOrLast(matchingDays, 3);
            case LAST -> matchingDays.get(matchingDays.size() - 1);
        };
    }

    private LocalDate getByIndexOrLast(List<LocalDate> dates, int index) {
        if (index < dates.size()) {
            return dates.get(index);
        }
        return dates.get(dates.size() - 1);
    }
}
