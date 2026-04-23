package com.alok.monthlydashboard.service.impl;

import com.alok.monthlydashboard.dto.task.*;
import com.alok.monthlydashboard.entity.Category;
import com.alok.monthlydashboard.entity.Task;
import com.alok.monthlydashboard.entity.TaskFixedDate;
import com.alok.monthlydashboard.entity.TaskRecurrenceRule;
import com.alok.monthlydashboard.entity.User;
import com.alok.monthlydashboard.entity.enums.RecurrenceType;
import com.alok.monthlydashboard.exception.ConflictException;
import com.alok.monthlydashboard.exception.ResourceNotFoundException;
import com.alok.monthlydashboard.exception.ValidationException;
import com.alok.monthlydashboard.repository.CategoryRepository;
import com.alok.monthlydashboard.repository.TaskCompletionRepository;
import com.alok.monthlydashboard.repository.TaskFixedDateRepository;
import com.alok.monthlydashboard.repository.TaskRecurrenceRuleRepository;
import com.alok.monthlydashboard.repository.TaskRepository;
import com.alok.monthlydashboard.repository.UserRepository;
import com.alok.monthlydashboard.service.TaskService;
import com.alok.monthlydashboard.util.TaskMapper;
import com.alok.monthlydashboard.util.TaskValidationHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final int MAX_ACTIVE_TASKS = 15;

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TaskRecurrenceRuleRepository recurrenceRuleRepository;
    private final TaskFixedDateRepository taskFixedDateRepository;
    private final TaskCompletionRepository taskCompletionRepository;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            TaskRecurrenceRuleRepository recurrenceRuleRepository,
            TaskFixedDateRepository taskFixedDateRepository,
            TaskCompletionRepository taskCompletionRepository
    ) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.recurrenceRuleRepository = recurrenceRuleRepository;
        this.taskFixedDateRepository = taskFixedDateRepository;
        this.taskCompletionRepository = taskCompletionRepository;
    }

    @Override
    public TaskDetailResponse createTask(CreateTaskRequest request) {
        validateDates(request.startDate(), request.endDate());
        TaskValidationHelper.validateRule(request.recurrenceType(), request.rule());

        Category category = getCategoryForCurrentUser(request.categoryId());
        User user = getDefaultUser();

        long activeCount = taskRepository.countByUserIdAndIsActive(DEFAULT_USER_ID, true);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new ValidationException("Maximum of 15 active tasks allowed");
        }

        Task task = new Task();
        task.setUser(user);
        task.setCategory(category);
        task.setName(request.name());
        task.setDescription(request.description());
        task.setRecurrenceType(mapRecurrenceType(request.recurrenceType()));
        task.setStartDate(request.startDate());
        task.setEndDate(request.endDate());
        task.setActive(true);

        TaskRecurrenceRule rule = buildRuleEntity(request.rule());
        task.setRecurrenceRule(rule);

        if (request.recurrenceType() == RecurrenceType.FIXED_DATE) {
            for (Integer day : request.rule().fixedDates()) {
                TaskFixedDate fixedDate = new TaskFixedDate();
                fixedDate.setDayOfMonth(day);
                task.addFixedDate(fixedDate);
            }
        }

        Task saved = taskRepository.save(task);
        return TaskMapper.toTaskDetailResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskSummaryResponse> getTasks(Long categoryId, Boolean active) {
        List<Task> tasks;

        if (categoryId != null && active != null) {
            ensureCategoryBelongsToCurrentUser(categoryId);
            tasks = taskRepository.findByUserIdAndCategoryIdAndIsActiveOrderByNameAsc(DEFAULT_USER_ID, categoryId, active);
        } else if (categoryId != null) {
            ensureCategoryBelongsToCurrentUser(categoryId);
            tasks = taskRepository.findByUserIdAndCategoryIdOrderByNameAsc(DEFAULT_USER_ID, categoryId);
        } else if (active != null) {
            tasks = taskRepository.findByUserIdAndIsActiveOrderByNameAsc(DEFAULT_USER_ID, active);
        } else {
            tasks = taskRepository.findByUserIdOrderByNameAsc(DEFAULT_USER_ID);
        }

        return tasks.stream()
                .map(TaskMapper::toTaskSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDetailResponse getTask(Long taskId) {
        Task task = getTaskForCurrentUser(taskId);
        return TaskMapper.toTaskDetailResponse(task);
    }

    @Override
    public TaskMutationResponse updateTask(Long taskId, UpdateTaskRequest request) {
        validateDates(request.startDate(), request.endDate());
        TaskValidationHelper.validateRule(request.recurrenceType(), request.rule());

        Task task = getTaskForCurrentUser(taskId);
        Category category = getCategoryForCurrentUser(request.categoryId());

        boolean activatingInactiveTask = !task.isActive() && request.isActive();
        if (activatingInactiveTask) {
            long activeCount = taskRepository.countByUserIdAndIsActive(DEFAULT_USER_ID, true);
            if (activeCount >= MAX_ACTIVE_TASKS) {
                throw new ValidationException("Maximum of 15 active tasks allowed");
            }
        }

        task.setCategory(category);
        task.setName(request.name());
        task.setDescription(request.description());
        task.setRecurrenceType(mapRecurrenceType(request.recurrenceType()));
        task.setStartDate(request.startDate());
        task.setEndDate(request.endDate());
        task.setActive(request.isActive());

        TaskRecurrenceRule rule = task.getRecurrenceRule();
        if (rule == null) {
            rule = new TaskRecurrenceRule();
            task.setRecurrenceRule(rule);
        }
        applyRuleValues(rule, request.rule());

        task.clearFixedDates();
        if (request.recurrenceType() == RecurrenceType.FIXED_DATE) {
            for (Integer day : request.rule().fixedDates()) {
                TaskFixedDate fixedDate = new TaskFixedDate();
                fixedDate.setDayOfMonth(day);
                task.addFixedDate(fixedDate);
            }
        }

        Task saved = taskRepository.save(task);

        return new TaskMutationResponse(
                saved.getId(),
                saved.isActive(),
                "Task updated successfully",
                LocalDateTime.now()
        );
    }

    @Override
    public TaskMutationResponse activateTask(Long taskId) {
        Task task = getTaskForCurrentUser(taskId);

        if (task.isActive()) {
            return new TaskMutationResponse(task.getId(), true, "Task already active", LocalDateTime.now());
        }

        long activeCount = taskRepository.countByUserIdAndIsActive(DEFAULT_USER_ID, true);
        if (activeCount >= MAX_ACTIVE_TASKS) {
            throw new ConflictException("Cannot activate task. Maximum of 15 active tasks allowed");
        }

        task.setActive(true);
        taskRepository.save(task);

        return new TaskMutationResponse(task.getId(), true, "Task activated successfully", LocalDateTime.now());
    }

    @Override
    public TaskMutationResponse deactivateTask(Long taskId) {
        Task task = getTaskForCurrentUser(taskId);

        if (!task.isActive()) {
            return new TaskMutationResponse(task.getId(), false, "Task already inactive", LocalDateTime.now());
        }

        task.setActive(false);
        taskRepository.save(task);

        return new TaskMutationResponse(task.getId(), false, "Task deactivated successfully", LocalDateTime.now());
    }

    @Override
    public TaskMutationResponse deleteTask(Long taskId, boolean deleteHistory, boolean confirm) {
        Task task = getTaskForCurrentUser(taskId);

        if (deleteHistory && !confirm) {
            throw new ValidationException("Explicit confirmation is required to delete task history");
        }

        if (!deleteHistory) {
            task.setActive(false);
            taskRepository.save(task);
            return new TaskMutationResponse(
                    task.getId(),
                    false,
                    "Task deleted successfully. History preserved.",
                    LocalDateTime.now()
            );
        }

        taskCompletionRepository.deleteAll(task.getCompletions());
        taskRepository.delete(task);

        return new TaskMutationResponse(
                taskId,
                false,
                "Task and history deleted successfully",
                LocalDateTime.now()
        );
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationException("endDate must be on or after startDate");
        }
    }

    private Category getCategoryForCurrentUser(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getId().equals(DEFAULT_USER_ID))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private void ensureCategoryBelongsToCurrentUser(Long categoryId) {
        if (!categoryRepository.existsByIdAndUserId(categoryId, DEFAULT_USER_ID)) {
            throw new ResourceNotFoundException("Category not found with id: " + categoryId);
        }
    }

    private Task getTaskForCurrentUser(Long taskId) {
        return taskRepository.findByIdAndUserId(taskId, DEFAULT_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    private User getDefaultUser() {
        return userRepository.findById(DEFAULT_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Default user not found with id: " + DEFAULT_USER_ID));
    }

    private com.alok.monthlydashboard.entity.enums.RecurrenceType mapRecurrenceType(RecurrenceType dtoType) {
        return com.alok.monthlydashboard.entity.enums.RecurrenceType.valueOf(dtoType.name());
    }

    private TaskRecurrenceRule buildRuleEntity(TaskRuleRequest request) {
        TaskRecurrenceRule rule = new TaskRecurrenceRule();
        applyRuleValues(rule, request);
        return rule;
    }

    private void applyRuleValues(TaskRecurrenceRule rule, TaskRuleRequest request) {
        rule.setIntervalValue(request.intervalValue());
        rule.setIntervalUnit(request.intervalUnit() == null ? null :
                com.alok.monthlydashboard.entity.enums.IntervalUnit.valueOf(request.intervalUnit().name()));
        rule.setWeekday(request.weekday() == null ? null :
                com.alok.monthlydashboard.entity.enums.Weekday.valueOf(request.weekday().name()));
        rule.setWeekOfMonth(request.weekOfMonth() == null ? null :
                com.alok.monthlydashboard.entity.enums.WeekOfMonth.valueOf(request.weekOfMonth().name()));
        rule.setFallbackToLastDay(request.fallbackToLastDay() == null || request.fallbackToLastDay());
    }
}