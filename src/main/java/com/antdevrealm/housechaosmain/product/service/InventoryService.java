package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;

import java.util.List;

public interface InventoryService {

    /** Validates and atomically deducts stock for every item. Used by OrderService.confirm(). */
    void deductStock(List<OrderItemEntity> items);

    /** Validates stock for every item without mutating. Used by OrderService.create(). */
    void assertSufficientStock(List<OrderItemEntity> items);

    /** Single-item validation without mutation. Used by CartService.addOneToCart(). */
    void assertSufficientStock(ProductEntity product, int requestedQuantity);
}
