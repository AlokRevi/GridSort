package com.alok.monthlydashboard.repository;

import com.alok.monthlydashboard.entity.TaskRecurrenceRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRecurrenceRuleRepository extends JpaRepository<TaskRecurrenceRule, Long> {
    Optional<TaskRecurrenceRule> findByTaskId(Long taskId);
}