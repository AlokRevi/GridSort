package com.alok.monthlydashboard.controller;

import com.alok.monthlydashboard.dto.task.*;
import com.alok.monthlydashboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskDetailResponse createTask(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.createTask(request);
    }

    @GetMapping
    public List<TaskSummaryResponse> getTasks(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active
    ) {
        return taskService.getTasks(categoryId, active);
    }

    @GetMapping("/{taskId}")
    public TaskDetailResponse getTask(@PathVariable Long taskId) {
        return taskService.getTask(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskMutationResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.updateTask(taskId, request);
    }

    @PatchMapping("/{taskId}/activate")
    public TaskMutationResponse activateTask(@PathVariable Long taskId) {
        return taskService.activateTask(taskId);
    }

    @PatchMapping("/{taskId}/deactivate")
    public TaskMutationResponse deactivateTask(@PathVariable Long taskId) {
        return taskService.deactivateTask(taskId);
    }

    @DeleteMapping("/{taskId}")
    public TaskMutationResponse deleteTask(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "false") boolean deleteHistory,
            @RequestParam(defaultValue = "false") boolean confirm
    ) {
        return taskService.deleteTask(taskId, deleteHistory, confirm);
    }
}