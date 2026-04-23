package com.alok.monthlydashboard.util;

import com.alok.monthlydashboard.dto.category.CategoryResponse;
import com.alok.monthlydashboard.entity.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getColor(),
                category.getTasks() == null ? 0 : category.getTasks().size(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}