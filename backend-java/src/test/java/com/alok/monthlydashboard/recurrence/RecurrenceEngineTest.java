package com.alok.monthlydashboard.recurrence;

import com.alok.monthlydashboard.common.enums.IntervalUnit;
import com.alok.monthlydashboard.common.enums.RecurrenceType;
import com.alok.monthlydashboard.common.enums.WeekOfMonth;
import com.alok.monthlydashboard.common.enums.Weekday;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecurrenceEngineTest {

    private final RecurrenceEngine engine = new RecurrenceEngine();

    @Test
    void fixedDatesGenerateInSortedOrderAndCollapseDuplicateFallbackDates() {
        RecurrenceRule rule = fixedDateRule(true, 10, 30, 31);

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 28)
        )).containsExactly(
                LocalDate.of(2026, 2, 10),
                LocalDate.of(2026, 2, 28)
        );
    }

    @Test
    void fixedDateSkipsImpossibleDatesWhenFallbackIsDisabled() {
        RecurrenceRule rule = fixedDateRule(false, 31);

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).isEmpty();
    }

    @Test
    void intervalContinuesCadenceAcrossMonthBoundary() {
        RecurrenceRule rule = intervalRule(
                LocalDate.of(2026, 3, 30),
                7,
                IntervalUnit.DAYS
        );

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).containsExactly(
                LocalDate.of(2026, 4, 6),
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 27)
        );
    }

    @Test
    void weeklyIntervalContinuesCadenceAcrossMonthBoundary() {
        RecurrenceRule rule = intervalRule(
                LocalDate.of(2026, 3, 30),
                1,
                IntervalUnit.WEEKS
        );

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).containsExactly(
                LocalDate.of(2026, 4, 6),
                LocalDate.of(2026, 4, 13),
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 27)
        );
    }

    @Test
    void monthlyIntervalFromJanThirtyFirstPreservesAnchorAcrossShortMonths() {
        RecurrenceRule rule = intervalRule(
                LocalDate.of(2026, 1, 31),
                1,
                IntervalUnit.MONTHS
        );

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 4, 30)
        )).containsExactly(
                LocalDate.of(2026, 2, 28),
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 4, 30)
        );
    }

    @Test
    void weekdayRuleRespectsStartAndEndDateBoundaries() {
        RecurrenceRule rule = new RecurrenceRule(
                RecurrenceType.WEEKDAY,
                LocalDate.of(2026, 4, 10),
                LocalDate.of(2026, 5, 5),
                true,
                List.of(),
                true,
                null,
                null,
                Weekday.MONDAY,
                WeekOfMonth.FIRST
        );

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 6, 30)
        )).containsExactly(LocalDate.of(2026, 5, 4));
    }

    @Test
    void inactiveRuleGeneratesNoDates() {
        RecurrenceRule rule = new RecurrenceRule(
                RecurrenceType.FIXED_DATE,
                LocalDate.of(2026, 1, 1),
                null,
                false,
                List.of(15),
                true,
                null,
                null,
                null,
                null
        );

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        )).isEmpty();
    }

    @Test
    void invertedDateRangeGeneratesNoDates() {
        RecurrenceRule rule = fixedDateRule(true, 15);

        assertThat(engine.generateOccurrenceDatesBetween(
                rule,
                LocalDate.of(2026, 5, 31),
                LocalDate.of(2026, 5, 1)
        )).isEmpty();
    }

    private RecurrenceRule fixedDateRule(boolean fallbackToLastDay, Integer... fixedDates) {
        return new RecurrenceRule(
                RecurrenceType.FIXED_DATE,
                LocalDate.of(2026, 1, 1),
                null,
                true,
                List.of(fixedDates),
                fallbackToLastDay,
                null,
                null,
                null,
                null
        );
    }

    private RecurrenceRule intervalRule(LocalDate startDate, int intervalValue, IntervalUnit intervalUnit) {
        return new RecurrenceRule(
                RecurrenceType.INTERVAL,
                startDate,
                null,
                true,
                List.of(),
                true,
                intervalValue,
                intervalUnit,
                null,
                null
        );
    }
}
