package com.example.product_service.kafka.event;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderItem {
    private String id;
    private String productId;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal discountAmount;
    private BigDecimal lineTotal;
}
