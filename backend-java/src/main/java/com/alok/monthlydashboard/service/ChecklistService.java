package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.checklist.CompleteTaskRequest;
import com.alok.monthlydashboard.dto.checklist.CompletionResponse;
import com.alok.monthlydashboard.dto.checklist.TodayChecklistResponse;

import java.time.LocalDate;

public interface ChecklistService {
    TodayChecklistResponse getTodayChecklist();
    CompletionResponse completeTask(Long taskId, CompleteTaskRequest request);
    void undoCompletion(Long taskId, LocalDate occurrenceDate);
}