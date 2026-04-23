package com.alok.monthlydashboard.repository;

import com.alok.monthlydashboard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserIdOrderByNameAsc(Long userId);

    List<Task> findByUserIdAndCategoryIdOrderByNameAsc(Long userId, Long categoryId);

    List<Task> findByUserIdAndIsActiveOrderByNameAsc(Long userId, boolean isActive);

    List<Task> findByUserIdAndCategoryIdAndIsActiveOrderByNameAsc(Long userId, Long categoryId, boolean isActive);

    long countByUserIdAndIsActive(Long userId, boolean isActive);

    Optional<Task> findByIdAndUserId(Long taskId, Long userId);
}