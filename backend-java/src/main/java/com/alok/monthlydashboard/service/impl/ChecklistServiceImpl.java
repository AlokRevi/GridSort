package com.alok.monthlydashboard.service.impl;

import com.alok.monthlydashboard.dto.checklist.*;
import com.alok.monthlydashboard.dto.dashboard.OccurrenceResponse;
import com.alok.monthlydashboard.entity.Task;
import com.alok.monthlydashboard.entity.TaskCompletion;
import com.alok.monthlydashboard.exception.ConflictException;
import com.alok.monthlydashboard.exception.ResourceNotFoundException;
import com.alok.monthlydashboard.exception.ValidationException;
import com.alok.monthlydashboard.repository.TaskCompletionRepository;
import com.alok.monthlydashboard.repository.TaskRepository;
import com.alok.monthlydashboard.service.ChecklistService;
import com.alok.monthlydashboard.service.RecurrenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ChecklistServiceImpl implements ChecklistService {

    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final RecurrenceService recurrenceService;

    public ChecklistServiceImpl(
            TaskRepository taskRepository,
            TaskCompletionRepository taskCompletionRepository,
            RecurrenceService recurrenceService
    ) {
        this.taskRepository = taskRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.recurrenceService = recurrenceService;
    }

    @Override
    public TodayChecklistResponse getTodayChecklist() {
        LocalDate today = LocalDate.now();

        // V1 single-user shortcut
        Long userId = 1L;

        List<Task> activeTasks = taskRepository.findByUserIdAndIsActiveOrderByNameAsc(userId, true);
        List<ChecklistItemResponse> items = new ArrayList<>();

        for (Task task : activeTasks) {
            List<LocalDate> candidateDates = collectCandidateDates(task, today);

            for (LocalDate occurrenceDate : candidateDates) {
                if (taskCompletionRepository.existsByTaskIdAndOccurrenceDate(task.getId(), occurrenceDate)) {
                    continue;
                }

                OccurrenceStatus status = occurrenceDate.isEqual(today)
                        ? OccurrenceStatus.DUE_TODAY
                        : OccurrenceStatus.OVERDUE;

                items.add(new ChecklistItemResponse(
                        task.getId(),
                        task.getName(),
                        task.getCategory().getId(),
                        task.getCategory().getName(),
                        occurrenceDate,
                        status
                ));
            }
        }

        items.sort(
                Comparator.comparing(ChecklistItemResponse::occurrenceDate)
                        .thenComparing(ChecklistItemResponse::taskName)
        );

        return new TodayChecklistResponse(today, items);
    }

    @Override
    public CompletionResponse completeTask(Long taskId, CompleteTaskRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        LocalDate today = LocalDate.now();
        LocalDate occurrenceDate = request.occurrenceDate();
        LocalDate completionDate = request.completionDate();

        if (!task.isActive()) {
            throw new ValidationException("Inactive task cannot be completed");
        }

        if (occurrenceDate.isAfter(today)) {
            throw new ValidationException("Future occurrence cannot be completed");
        }

        if (completionDate.isAfter(today)) {
            throw new ValidationException("Future completion is not allowed");
        }

        if (completionDate.isBefore(occurrenceDate)) {
            throw new ValidationException("Completion date cannot be before occurrence date");
        }

        if (!recurrenceService.isValidOccurrence(taskId, occurrenceDate)) {
            throw new ValidationException("Provided occurrence date is not valid for this task");
        }

        if (taskCompletionRepository.existsByTaskIdAndOccurrenceDate(taskId, occurrenceDate)) {
            throw new ConflictException("This task occurrence is already completed");
        }

        TaskCompletion completion = new TaskCompletion();
        completion.setTask(task);
        completion.setOccurrenceDate(occurrenceDate);
        completion.setCompletionDate(completionDate);

        taskCompletionRepository.save(completion);

        return new CompletionResponse(
                taskId,
                occurrenceDate,
                completionDate,
                OccurrenceStatus.COMPLETED,
                "Task marked complete"
        );
    }

    @Override
    public void undoCompletion(Long taskId, LocalDate occurrenceDate) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        taskCompletionRepository.findByTaskIdAndOccurrenceDate(task.getId(), occurrenceDate)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Completion not found for task id " + taskId + " and occurrence date " + occurrenceDate
                ));

        taskCompletionRepository.deleteByTaskIdAndOccurrenceDate(taskId, occurrenceDate);
    }

    /**
     * Collect dates that could appear in today's checklist:
     * - overdue dates from previous month
     * - overdue dates earlier this month
     * - due today
     *
     * Because tasks can spill across month boundaries, we check:
     * - previous month
     * - current month
     */
    private List<LocalDate> collectCandidateDates(Task task, LocalDate today) {
        List<LocalDate> all = new ArrayList<>();

        YearMonth currentMonth = YearMonth.from(today);
        YearMonth previousMonth = currentMonth.minusMonths(1);

        all.addAll(recurrenceService.generateOccurrenceDatesForMonth(
                task.getId(),
                previousMonth.getYear(),
                previousMonth.getMonthValue()
        ));

        all.addAll(recurrenceService.generateOccurrenceDatesForMonth(
                task.getId(),
                currentMonth.getYear(),
                currentMonth.getMonthValue()
        ));

        return all.stream()
                .filter(date -> !date.isAfter(today)) // only today or earlier
                .sorted()
                .toList();
    }
}