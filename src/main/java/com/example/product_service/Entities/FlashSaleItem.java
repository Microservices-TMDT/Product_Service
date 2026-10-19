package com.example.product_service.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItem extends BaseEntity {

    @Column(name = "flash_sale_id")
    private String flashSaleId;

    @Column(name = "variant_id")
    private String variantId;

    @Column(name = "sale_price")
    private BigDecimal salePrice;

    @Column(name = "quantity_limit")
    private Integer quantityLimit;

    @Column(name = "quantity_sold")
    private Integer quantitySold = 0;
}
