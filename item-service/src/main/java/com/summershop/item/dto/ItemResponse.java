package com.summershop.item.dto;

import com.summershop.item.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    private String id;
    private String upc;
    private String name;
    private String description;
    private String category;
    private BigDecimal price;
    private String brand;
    private Inventory inventory;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}