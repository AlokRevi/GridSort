package com.alok.monthlydashboard.controller;

import com.alok.monthlydashboard.dto.checklist.CompleteTaskRequest;
import com.alok.monthlydashboard.dto.checklist.CompletionResponse;
import com.alok.monthlydashboard.dto.checklist.TodayChecklistResponse;
import com.alok.monthlydashboard.service.ChecklistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1")
public class ChecklistController {

    private final ChecklistService checklistService;

    public ChecklistController(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @GetMapping("/checklist/today")
    public TodayChecklistResponse getTodayChecklist() {
        return checklistService.getTodayChecklist();
    }

    @PostMapping("/tasks/{taskId}/completions")
    @ResponseStatus(HttpStatus.CREATED)
    public CompletionResponse completeTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CompleteTaskRequest request
    ) {
        return checklistService.completeTask(taskId, request);
    }

    @DeleteMapping("/tasks/{taskId}/completions/{occurrenceDate}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void undoCompletion(
            @PathVariable Long taskId,
            @PathVariable LocalDate occurrenceDate
    ) {
        checklistService.undoCompletion(taskId, occurrenceDate);
    }
}