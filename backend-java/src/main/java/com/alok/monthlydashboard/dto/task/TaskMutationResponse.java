package com.alok.monthlydashboard.dto.task;

import java.time.LocalDateTime;

public record TaskMutationResponse(
        Long id,
        boolean isActive,
        String message,
        LocalDateTime updatedAt
) {
}