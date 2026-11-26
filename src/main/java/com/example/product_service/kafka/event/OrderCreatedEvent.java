package com.example.product_service.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class OrderCreatedEvent {

    private String id;

    private String orderCode;

    private String userId;

    private String status;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private List<OrderItem> items;
}
