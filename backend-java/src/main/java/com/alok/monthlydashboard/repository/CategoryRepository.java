package com.alok.monthlydashboard.repository;

import com.alok.monthlydashboard.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByNameAsc(Long userId);
    boolean existsByIdAndUserId(Long categoryId, Long userId);
}