package com.alok.monthlydashboard.repository;

import com.alok.monthlydashboard.entity.TaskFixedDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskFixedDateRepository extends JpaRepository<TaskFixedDate, Long> {
    List<TaskFixedDate> findByTaskIdOrderByDayOfMonthAsc(Long taskId);
    void deleteByTaskId(Long taskId);
}