package com.alok.monthlydashboard.dto.export;

import java.time.LocalDateTime;
import java.util.List;

public record SetupSnapshotResponse(
        LocalDateTime exportedAt,
        String version,
        List<SetupSnapshotCategoryResponse> categories,
        List<SetupSnapshotTaskResponse> tasks
) {
}
