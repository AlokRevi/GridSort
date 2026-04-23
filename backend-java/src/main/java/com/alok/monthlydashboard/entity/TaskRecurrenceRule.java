package com.alok.monthlydashboard.entity;

import com.alok.monthlydashboard.entity.enums.IntervalUnit;
import com.alok.monthlydashboard.entity.enums.WeekOfMonth;
import com.alok.monthlydashboard.entity.enums.Weekday;
import jakarta.persistence.*;

@Entity
@Table(name = "task_recurrence_rules")
public class TaskRecurrenceRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @Column(name = "interval_value")
    private Integer intervalValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "interval_unit", length = 10)
    private IntervalUnit intervalUnit;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Weekday weekday;

    @Enumerated(EnumType.STRING)
    @Column(name = "week_of_month", length = 10)
    private WeekOfMonth weekOfMonth;

    @Column(name = "fallback_to_last_day", nullable = false)
    private boolean fallbackToLastDay = true;

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(Integer intervalValue) {
        this.intervalValue = intervalValue;
    }

    public IntervalUnit getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(IntervalUnit intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Weekday getWeekday() {
        return weekday;
    }

    public void setWeekday(Weekday weekday) {
        this.weekday = weekday;
    }

    public WeekOfMonth getWeekOfMonth() {
        return weekOfMonth;
    }

    public void setWeekOfMonth(WeekOfMonth weekOfMonth) {
        this.weekOfMonth = weekOfMonth;
    }

    public boolean isFallbackToLastDay() {
        return fallbackToLastDay;
    }

    public void setFallbackToLastDay(boolean fallbackToLastDay) {
        this.fallbackToLastDay = fallbackToLastDay;
    }
}