package com.example.product_service.mapper;

import com.example.product_service.Entities.Product;
import com.example.product_service.dto.request.CreateProductRequest;
import com.example.product_service.dto.request.UpdateProductRequest;
import com.example.product_service.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(CreateProductRequest productRequest);

    ProductResponse toProductResponse(Product product);

    void updateProductFromRequest(UpdateProductRequest request, @MappingTarget Product product);
}
