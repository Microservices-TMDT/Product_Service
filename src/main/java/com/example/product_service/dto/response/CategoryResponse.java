package com.example.product_service.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    private String id;
    private String name;
    private String parentId;
    private String description;
    @Builder.Default
    private String status = "ACTIVE";
}
