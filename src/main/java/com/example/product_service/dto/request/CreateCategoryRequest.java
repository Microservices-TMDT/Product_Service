package com.example.product_service.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCategoryRequest {

    private String name;

    private String parentId;

    private String description;

    @Builder.Default
    private String status = "ACTIVE";
}
