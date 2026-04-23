package com.alok.monthlydashboard.dto.dashboard;

import java.util.List;

public record ScaleBarResponse(
        List<Integer> anchors,
        int lastDay,
        String currentDateLabel
) {
}