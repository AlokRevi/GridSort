package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.task.*;

import java.util.List;

public interface TaskService {
    TaskDetailResponse createTask(CreateTaskRequest request);
    List<TaskSummaryResponse> getTasks(Long categoryId, Boolean active);
    TaskDetailResponse getTask(Long taskId);
    TaskMutationResponse updateTask(Long taskId, UpdateTaskRequest request);
    TaskMutationResponse activateTask(Long taskId);
    TaskMutationResponse deactivateTask(Long taskId);
    TaskMutationResponse deleteTask(Long taskId, boolean deleteHistory, boolean confirm);
}