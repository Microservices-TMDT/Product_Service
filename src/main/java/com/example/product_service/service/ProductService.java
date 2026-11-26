package com.example.product_service.service;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.dto.request.ProductFilter;
import com.example.product_service.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest productRequest);
    List<ProductResponse> getProducts();
    List<ProductResponse> searchProducts(ProductFilter productFilter);
    void lock(LockProductRequet lockProductRequet);
}
