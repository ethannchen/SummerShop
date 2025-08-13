package com.summershop.item.controller;

import com.summershop.item.dto.InventoryUpdateRequest;
import com.summershop.item.dto.ItemResponse;
import com.summershop.item.entity.Inventory;
import com.summershop.item.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItem(@PathVariable String id) {
        ItemResponse response = itemService.getItem(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upc/{upc}")
    public ResponseEntity<ItemResponse> getItemByUpc(@PathVariable String upc) {
        ItemResponse response = itemService.getItemByUpc(upc);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/inventory")
    public ResponseEntity<ItemResponse> updateInventory(
            @PathVariable String id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        ItemResponse response = itemService.updateInventory(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/inventory")
    public ResponseEntity<Inventory> getInventory(@PathVariable String id) {
        Inventory inventory = itemService.getInventory(id);
        return ResponseEntity.ok(inventory);
    }
}