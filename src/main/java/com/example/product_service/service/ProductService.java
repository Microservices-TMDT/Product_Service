package com.example.product_service.service;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.dto.request.ProductFilter;
import com.example.product_service.dto.request.SearchProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest productRequest);

    ProductResponse updateProduct(String productId, UpdateProductRequest request);

    void deleteProduct(String productId);

    List<ProductResponse> getProducts();

    List<ProductResponse> searchProducts(ProductFilter productFilter);

    PageResponse<ProductResponse> searchProducts(SearchProductRequest request);

    // Inventory lock
    void lock(LockProductRequet lockProductRequet);
}
