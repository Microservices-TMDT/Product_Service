package com.example.product_service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    private String categoryId;

    private String name;

    private String description;

    private String imageUrl;

    @Builder.Default
    private String status = "ACTIVE";
}
