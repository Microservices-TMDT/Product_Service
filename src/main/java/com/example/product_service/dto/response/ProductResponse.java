package com.example.product_service.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private String id;
    private String categoryId;
    private String name;
    private String description;
    private String imageUrl;
    @Builder.Default
    private String status = "ACTIVE";
}
