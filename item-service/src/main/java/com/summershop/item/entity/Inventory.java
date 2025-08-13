package com.summershop.item.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private Integer quantity;

    private Integer reservedQuantity = 0;

    private Integer availableQuantity;

    private String warehouse;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }
}