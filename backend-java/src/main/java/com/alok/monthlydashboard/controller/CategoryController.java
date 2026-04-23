package com.alok.monthlydashboard.controller;

import com.alok.monthlydashboard.dto.category.CategoryResponse;
import com.alok.monthlydashboard.dto.category.CreateCategoryRequest;
import com.alok.monthlydashboard.dto.category.UpdateCategoryRequest;
import com.alok.monthlydashboard.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse getCategory(@PathVariable Long categoryId) {
        return categoryService.getCategory(categoryId);
    }

    @PutMapping("/{categoryId}")
    public CategoryResponse updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {
        return categoryService.updateCategory(categoryId, request);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
    }
}