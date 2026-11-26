package com.example.product_service.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchProductRequest {

    private List<String> productIds;

    private String keyword;

    private String categoryId;

    // Pagination
    private int page = 0;
    private int size = 20;
}
