package com.example.product_service.controller;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest productRequest) {
        ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                .data(productService.createProduct(productRequest))
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
