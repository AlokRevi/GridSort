package com.alok.monthlydashboard.service.impl;

import com.alok.monthlydashboard.dto.dashboard.*;
import com.alok.monthlydashboard.dto.history.TaskHistoryItemResponse;
import com.alok.monthlydashboard.dto.history.TaskHistoryResponse;
import com.alok.monthlydashboard.entity.Category;
import com.alok.monthlydashboard.entity.Task;
import com.alok.monthlydashboard.entity.TaskCompletion;
import com.alok.monthlydashboard.repository.CategoryRepository;
import com.alok.monthlydashboard.repository.TaskCompletionRepository;
import com.alok.monthlydashboard.repository.TaskRepository;
import com.alok.monthlydashboard.service.HistoryService;
import com.alok.monthlydashboard.service.RecurrenceService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class HistoryServiceImpl implements HistoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final RecurrenceService recurrenceService;

    public HistoryServiceImpl(
            CategoryRepository categoryRepository,
            TaskRepository taskRepository,
            TaskCompletionRepository taskCompletionRepository,
            RecurrenceService recurrenceService
    ) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.taskCompletionRepository = taskCompletionRepository;
        this.recurrenceService = recurrenceService;
    }

    @Override
    public MonthlyDashboardResponse getPastMonth(int year, int month) {
        Long userId = 1L; // V1 single-user shortcut

        YearMonth targetMonth = YearMonth.of(year, month);
        LocalDate today = LocalDate.now();

        ScaleBarResponse scaleBar = buildScaleBar(targetMonth);
        List<DayStripItemResponse> dayStrip = buildDayStrip(targetMonth, today);
        List<DashboardCategoryResponse> categoryResponses = buildCategoryResponses(userId, targetMonth);

        return new MonthlyDashboardResponse(
                year,
                month,
                buildMonthLabel(targetMonth),
                today,
                true,
                scaleBar,
                dayStrip,
                categoryResponses
        );
    }

    @Override
    public TaskHistoryResponse getTaskHistory(Long taskId) {
        Long userId = 1L; // V1 single-user shortcut

        Task task = taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new com.alok.monthlydashboard.exception.ResourceNotFoundException(
                        "Task not found with id: " + taskId
                ));

        List<TaskCompletion> completions = taskCompletionRepository.findByTaskIdOrderByOccurrenceDateAsc(taskId);

        List<TaskHistoryItemResponse> historyItems = completions.stream()
                .map(completion -> new TaskHistoryItemResponse(
                        completion.getOccurrenceDate(),
                        completion.getCompletionDate(),
                        com.alok.monthlydashboard.dto.checklist.OccurrenceStatus.COMPLETED
                ))
                .toList();

        return new TaskHistoryResponse(
                task.getId(),
                task.getName(),
                historyItems
        );
    }

    private List<DashboardCategoryResponse> buildCategoryResponses(Long userId, YearMonth targetMonth) {
        List<Category> categories = categoryRepository.findByUserIdOrderByNameAsc(userId);
        List<DashboardCategoryResponse> results = new ArrayList<>();

        for (Category category : categories) {
            List<Task> tasks = taskRepository.findByUserIdAndCategoryIdOrderByNameAsc(userId, category.getId());

            List<DashboardTaskResponse> taskResponses = new ArrayList<>();

            for (Task task : tasks) {
                List<OccurrenceResponse> occurrences = recurrenceService.generateOccurrencesForMonth(
                        task.getId(),
                        targetMonth.getYear(),
                        targetMonth.getMonthValue()
                );

                taskResponses.add(new DashboardTaskResponse(
                        task.getId(),
                        task.getName(),
                        task.getRecurrenceType(),
                        occurrences
                ));
            }

            results.add(new DashboardCategoryResponse(
                    category.getId(),
                    category.getName(),
                    taskResponses
            ));
        }

        return results;
    }

    private ScaleBarResponse buildScaleBar(YearMonth targetMonth) {
        int lastDay = targetMonth.lengthOfMonth();
        List<Integer> anchors = List.of(1, 8, 15, 22, lastDay);

        String currentDateLabel = targetMonth.getMonth()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                + " "
                + targetMonth.getYear();

        return new ScaleBarResponse(
                anchors,
                lastDay,
                currentDateLabel
        );
    }

    private List<DayStripItemResponse> buildDayStrip(YearMonth targetMonth, LocalDate today) {
        List<DayStripItemResponse> items = new ArrayList<>();

        for (int day = 1; day <= targetMonth.lengthOfMonth(); day++) {
            LocalDate date = targetMonth.atDay(day);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            items.add(new DayStripItemResponse(
                    date,
                    day,
                    dayOfWeek.name(),
                    date.equals(today),
                    isWeekend(dayOfWeek)
            ));
        }

        return items;
    }

    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private String buildMonthLabel(YearMonth targetMonth) {
        return targetMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " "
                + targetMonth.getYear();
    }
}