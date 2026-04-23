package com.alok.monthlydashboard.service.impl;

import com.alok.monthlydashboard.dto.category.CategoryResponse;
import com.alok.monthlydashboard.dto.category.CreateCategoryRequest;
import com.alok.monthlydashboard.dto.category.UpdateCategoryRequest;
import com.alok.monthlydashboard.entity.Category;
import com.alok.monthlydashboard.entity.User;
import com.alok.monthlydashboard.exception.ConflictException;
import com.alok.monthlydashboard.exception.ResourceNotFoundException;
import com.alok.monthlydashboard.repository.CategoryRepository;
import com.alok.monthlydashboard.repository.UserRepository;
import com.alok.monthlydashboard.service.CategoryService;
import com.alok.monthlydashboard.util.CategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final Long DEFAULT_USER_ID = 1L;

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryServiceImpl(
            CategoryRepository categoryRepository,
            UserRepository userRepository
    ) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        User user = getDefaultUser();

        Category category = new Category();
        category.setUser(user);
        category.setName(request.name());
        category.setColor(request.color());

        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findByUserIdOrderByNameAsc(DEFAULT_USER_ID)
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long categoryId) {
        Category category = getCategoryForCurrentUser(categoryId);
        return CategoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long categoryId, UpdateCategoryRequest request) {
        Category category = getCategoryForCurrentUser(categoryId);
        category.setName(request.name());
        category.setColor(request.color());

        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryForCurrentUser(categoryId);

        if (!category.getTasks().isEmpty()) {
            throw new ConflictException("Category cannot be deleted while tasks still belong to it");
        }

        categoryRepository.delete(category);
    }

    private Category getCategoryForCurrentUser(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getUser().getId().equals(DEFAULT_USER_ID))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryId));
    }

    private User getDefaultUser() {
        return userRepository.findById(DEFAULT_USER_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Default user not found with id: " + DEFAULT_USER_ID));
    }
}