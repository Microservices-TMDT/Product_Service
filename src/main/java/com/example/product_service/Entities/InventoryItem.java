package com.example.product_service.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem extends BaseEntity {

    @Column(name = "inventory_id")
    private String inventoryId;

    @Column(name = "variant_id")
    private String variantId;

    @Column(name = "quantity")
    private Integer quantity = 0;
}
