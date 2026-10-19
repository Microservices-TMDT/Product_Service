package com.example.product_service.service;

import com.example.product_service.dto.request.CreateCategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse createCategory(CreateCategoryRequest categoryRequest);
}
