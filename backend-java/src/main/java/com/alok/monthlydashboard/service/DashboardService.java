package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.dashboard.MonthlyDashboardResponse;

public interface DashboardService {
    MonthlyDashboardResponse getMonthlyDashboard(int year, int month);
}