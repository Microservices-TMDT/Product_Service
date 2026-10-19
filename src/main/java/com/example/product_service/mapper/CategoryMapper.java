package com.example.product_service.mapper;

import com.example.product_service.Entities.Category;
import com.example.product_service.dto.request.CreateCategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CreateCategoryRequest categoryRequest);
    CategoryResponse toCategoryResponse(Category category);
}
