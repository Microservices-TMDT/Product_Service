package com.example.product_service.controller;

import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.LockProductRequet;
import com.example.product_service.dto.request.ProductFilter;
import com.example.product_service.dto.request.SearchProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.ApiResponse;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.dto.response.ProductResponse;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/products")
public class ProductController {

        private final ProductService productService;

        @PostMapping

        public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
                        @Valid @RequestBody CreateProductRequest productRequest) {
                ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                                .data(productService.createProduct(productRequest))
                                .message("Product created successfully")
                                .build();
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @PutMapping("/{productId}")
        public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
                        @PathVariable String productId,
                        @Valid @RequestBody UpdateProductRequest request) {
                ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
                                .data(productService.updateProduct(productId, request))
                                .message("Product updated successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @DeleteMapping("/{productId}")
        public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable String productId) {
                productService.deleteProduct(productId);
                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .message("Product deleted successfully")
                                .build();
                return ResponseEntity.ok(response);
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts() {
                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                                .data(productService.getProducts())
                                .build();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/search") // data vào là list -> url bị giới hạn
        public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProductsBasic(
                        @RequestBody ProductFilter productFilter) {
                List<ProductResponse> productResponseList = productService.searchProducts(productFilter);
                ApiResponse<List<ProductResponse>> response = ApiResponse.<List<ProductResponse>>builder()
                                .data(productResponseList)
                                .message("success")
                                .build();
                return ResponseEntity.ok(response);
        }

        @PostMapping("/search/page")
        public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
                        @RequestBody SearchProductRequest request) {
                ApiResponse<PageResponse<ProductResponse>> response = ApiResponse
                                .<PageResponse<ProductResponse>>builder()
                                .data(productService.searchProducts(request))
                                .message("success")
                                .build();
                return ResponseEntity.ok(response);
        }

        // lock – inventory
        @PutMapping("/lock")
        public ResponseEntity<ApiResponse<Void>> lockProducts(
                        @Valid @RequestBody LockProductRequet lockProductRequet) {
                productService.lock(lockProductRequet);
                ApiResponse<Void> response = ApiResponse.<Void>builder()
                                .message("Products locked successfully")
                                .build();
                return ResponseEntity.ok(response);
        }
}
