package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.dashboard.MonthlyDashboardResponse;
import com.alok.monthlydashboard.dto.history.TaskHistoryResponse;

public interface HistoryService {
    MonthlyDashboardResponse getPastMonth(int year, int month);
    TaskHistoryResponse getTaskHistory(Long taskId);
}