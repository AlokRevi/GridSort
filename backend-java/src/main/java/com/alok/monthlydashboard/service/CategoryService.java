package com.alok.monthlydashboard.service;

import com.alok.monthlydashboard.dto.category.CategoryResponse;
import com.alok.monthlydashboard.dto.category.CreateCategoryRequest;
import com.alok.monthlydashboard.dto.category.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategory(Long categoryId);
    CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request);
    void deleteCategory(Long categoryId);
}