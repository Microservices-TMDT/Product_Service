package com.example.product_service.kafka.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductUpdatedEvent {
    private String productId;
    private String name;
    private Integer price;
    private Integer stock;
    private String categoryId;
}
