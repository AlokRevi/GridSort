package com.alok.monthlydashboard.service.impl;

import com.alok.monthlydashboard.dto.dashboard.OccurrenceResponse;
import com.alok.monthlydashboard.entity.Task;
import com.alok.monthlydashboard.entity.TaskCompletion;
import com.alok.monthlydashboard.entity.TaskRecurrenceRule;
import com.alok.monthlydashboard.exception.ResourceNotFoundException;
import com.alok.monthlydashboard.recurrence.RecurrenceEngine;
import com.alok.monthlydashboard.recurrence.RecurrenceRule;
import com.alok.monthlydashboard.repository.TaskCompletionRepository;
import com.alok.monthlydashboard.repository.TaskRepository;
import com.alok.monthlydashboard.service.AppDateProvider;
import com.alok.monthlydashboard.service.RecurrenceService;
import org.springframework.stereotype.Service;
import com.alok.monthlydashboard.common.enums.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class RecurrenceServiceImpl implements RecurrenceService {

    private final RecurrenceEngine recurrenceEngine = new RecurrenceEngine();
    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final AppDateProvider appDateProvider;

    public RecurrenceServiceImpl(
            TaskRepository taskRepository,
            TaskCompletionRepository taskCompletionRepository,
            AppDateProvider appDateProvider
    ) {
        this.taskRepository = taskRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.appDateProvider = appDateProvider;
    }

    @Override
    public List<OccurrenceResponse> generateOccurrencesForMonth(Long taskId, int year, int month) {
        Task task = getTaskOrThrow(taskId);
        YearMonth targetMonth = YearMonth.of(year, month);
        return generateOccurrencesBetween(
                task,
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()
        );
    }

    @Override
    public List<OccurrenceResponse> generateOccurrencesBetween(
            Long taskId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return generateOccurrencesBetween(getTaskOrThrow(taskId), startDate, endDate);
    }

    @Override
    public List<OccurrenceResponse> generateOccurrencesBetween(
            Task task,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate today = appDateProvider.today();
        List<LocalDate> occurrenceDates = generateOccurrenceDatesBetween(task, startDate, endDate);
        List<OccurrenceResponse> responses = new ArrayList<>();

        for (LocalDate occurrenceDate : occurrenceDates) {
            Optional<TaskCompletion> completionOpt =
                    taskCompletionRepository.findByTaskIdAndOccurrenceDate(task.getId(), occurrenceDate);

            boolean completed = completionOpt.isPresent();
            LocalDate completionDate = completionOpt.map(TaskCompletion::getCompletionDate).orElse(null);

            OccurrenceStatus status = determineStatus(occurrenceDate, completed, today);

            responses.add(new OccurrenceResponse(
                    occurrenceDate,
                    occurrenceDate.getDayOfMonth(),
                    completed,
                    completionDate,
                    status
            ));
        }

        responses.sort(Comparator.comparing(OccurrenceResponse::occurrenceDate));
        return responses;
    }

    @Override
    public List<LocalDate> generateOccurrenceDatesForMonth(Long taskId, int year, int month) {
        Task task = getTaskOrThrow(taskId);
        YearMonth targetMonth = YearMonth.of(year, month);
        return generateOccurrenceDatesBetween(
                task,
                targetMonth.atDay(1),
                targetMonth.atEndOfMonth()
        );
    }

    @Override
    public List<LocalDate> generateOccurrenceDatesBetween(
            Long taskId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return generateOccurrenceDatesBetween(getTaskOrThrow(taskId), startDate, endDate);
    }

    @Override
    public List<LocalDate> generateOccurrenceDatesBetween(
            Task task,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return recurrenceEngine.generateOccurrenceDatesBetween(toRecurrenceRule(task), startDate, endDate);
    }

    @Override
    public boolean isValidOccurrence(Long taskId, LocalDate occurrenceDate) {
        Task task = getTaskOrThrow(taskId);

        // quick lifetime check
        if (occurrenceDate.isBefore(task.getStartDate())) {
            return false;
        }
        if (task.getEndDate() != null && occurrenceDate.isAfter(task.getEndDate())) {
            return false;
        }

        YearMonth targetMonth = YearMonth.from(occurrenceDate);
        List<LocalDate> datesForMonth =
                recurrenceEngine.generateOccurrenceDatesBetween(
                        toRecurrenceRule(task),
                        targetMonth.atDay(1),
                        targetMonth.atEndOfMonth()
                );

        return datesForMonth.contains(occurrenceDate);
    }

    private Task getTaskOrThrow(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private TaskRecurrenceRule getRuleOrThrow(Task task) {
        TaskRecurrenceRule rule = task.getRecurrenceRule();
        if (rule == null) {
            throw new ResourceNotFoundException(
                    "Recurrence rule not found for task id: " + task.getId()
            );
        }
        return rule;
    }

    private RecurrenceRule toRecurrenceRule(Task task) {
        TaskRecurrenceRule rule = task.getRecurrenceRule();
        if (task.getRecurrenceType() == RecurrenceType.INTERVAL || task.getRecurrenceType() == RecurrenceType.WEEKDAY) {
            rule = getRuleOrThrow(task);
        }

        return new RecurrenceRule(
                task.getRecurrenceType(),
                task.getStartDate(),
                task.getEndDate(),
                task.isActive(),
                task.getFixedDates().stream()
                        .map(fixedDate -> fixedDate.getDayOfMonth())
                        .toList(),
                rule == null || rule.isFallbackToLastDay(),
                rule == null ? null : rule.getIntervalValue(),
                rule == null || rule.getIntervalUnit() == null
                        ? null
                        : IntervalUnit.valueOf(rule.getIntervalUnit().name()),
                rule == null || rule.getWeekday() == null
                        ? null
                        : Weekday.valueOf(rule.getWeekday().name()),
                rule == null || rule.getWeekOfMonth() == null
                        ? null
                        : WeekOfMonth.valueOf(rule.getWeekOfMonth().name())
        );
    }

    private OccurrenceStatus determineStatus(LocalDate occurrenceDate, boolean completed, LocalDate today) {
        if (completed) {
            return OccurrenceStatus.COMPLETED;
        }
        if (occurrenceDate.isEqual(today)) {
            return OccurrenceStatus.DUE_TODAY;
        }
        if (occurrenceDate.isBefore(today)) {
            return OccurrenceStatus.OVERDUE;
        }
        return OccurrenceStatus.UPCOMING;
    }
}
