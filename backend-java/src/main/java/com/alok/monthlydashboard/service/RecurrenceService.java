package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.dashboard.OccurrenceResponse;

import java.time.LocalDate;
import java.util.List;

public interface RecurrenceService {
    List<OccurrenceResponse> generateOccurrencesForMonth(Long taskId, int year, int month);
    List<LocalDate> generateOccurrenceDatesForMonth(Long taskId, int year, int month);
    boolean isValidOccurrence(Long taskId, LocalDate occurrenceDate);
}