package com.example.product_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @NotEmpty
    private String name;

    @NotNull
    @Positive
    private Integer price;

    @NotNull
    @Positive
    private Integer stock;

    @NotEmpty
    private String categoryId;
}
