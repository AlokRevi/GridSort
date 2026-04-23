package com.alok.monthlydashboard.dto.history;

import java.util.List;

public record TaskHistoryResponse(
        Long taskId,
        String taskName,
        List<TaskHistoryItemResponse> history
) {
}