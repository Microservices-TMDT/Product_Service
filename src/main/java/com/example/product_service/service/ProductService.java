package com.example.product_service.service;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.response.ProductResponse;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest productRequest);
}
