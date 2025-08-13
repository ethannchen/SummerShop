package com.summershop.item.service;

import com.summershop.item.dto.InventoryUpdateRequest;
import com.summershop.item.dto.ItemResponse;
import com.summershop.item.entity.Inventory;
import com.summershop.item.entity.Item;
import com.summershop.item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemResponse getItem(String id) {
        log.info("Fetching item with id: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        return mapToResponse(item);
    }

    public ItemResponse getItemByUpc(String upc) {
        log.info("Fetching item with UPC: {}", upc);

        Item item = itemRepository.findByUpc(upc)
                .orElseThrow(() -> new RuntimeException("Item not found with UPC: " + upc));

        return mapToResponse(item);
    }

    public ItemResponse updateInventory(String id, InventoryUpdateRequest request) {
        log.info("Updating inventory for item: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        if (item.getInventory() == null) {
            item.setInventory(new Inventory());
        }

        Inventory inventory = item.getInventory();
        inventory.setQuantity(request.getQuantity());
        inventory.setWarehouse(request.getWarehouse());
        inventory.setLastUpdated(LocalDateTime.now());

        item.setUpdatedAt(LocalDateTime.now());

        Item updatedItem = itemRepository.save(item);
        return mapToResponse(updatedItem);
    }

    public Inventory getInventory(String id) {
        log.info("Fetching inventory for item: {}", id);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));

        if (item.getInventory() == null) {
            return new Inventory(0, 0, 0, null, LocalDateTime.now());
        }

        return item.getInventory();
    }

    private ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setUpc(item.getUpc());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setPrice(item.getPrice());
        response.setBrand(item.getBrand());
        response.setInventory(item.getInventory());
        response.setActive(item.getActive());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }
}