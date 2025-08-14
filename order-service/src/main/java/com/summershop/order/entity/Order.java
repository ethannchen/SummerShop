package com.summershop.order.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Table("orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class Order {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.findAndRegisterModules();
    }

    @PrimaryKeyColumn(name = "order_id", type = PrimaryKeyType.PARTITIONED)
    private UUID orderId;

    @Column("customer_id")
    private Long customerId;

    @Column("order_status")
    private String orderStatus;

    @Column("payment_status")
    private String paymentStatus;

    @Column("payment_id")
    private String paymentId;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("items_json")
    private String itemsJson;

    @Column("shipping_address")
    private String shippingAddress;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // Helper method to get items from JSON
    public List<OrderItem> getItemsFromJson() {
        if (itemsJson == null || itemsJson.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(itemsJson, new TypeReference<List<OrderItem>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing items JSON: {}", itemsJson, e);
            return List.of();
        }
    }

    // Helper method to set items as JSON
    public void setItemsAsJson(List<OrderItem> items) {
        if (items != null && !items.isEmpty()) {
            try {
                this.itemsJson = objectMapper.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                log.error("Error converting items to JSON", e);
                this.itemsJson = "[]";
            }
        } else {
            this.itemsJson = "[]";
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItem {
        private String itemId;
        private String itemName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}