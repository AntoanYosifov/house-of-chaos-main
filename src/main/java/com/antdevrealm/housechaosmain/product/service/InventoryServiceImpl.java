package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Autowired
    public InventoryServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void deductStock(List<OrderItemEntity> items) {
        for (OrderItemEntity item : items) {
            ProductEntity product = item.getProduct();
            guard(product, item.getQuantity());
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

    @Override
    public void assertSufficientStock(List<OrderItemEntity> items) {
        for (OrderItemEntity item : items) {
            guard(item.getProduct(), item.getQuantity());
        }
    }

    @Override
    public void assertSufficientStock(ProductEntity product, int requestedQuantity) {
        guard(product, requestedQuantity);
    }

    private void guard(ProductEntity product, int requested) {
        if (requested > product.getQuantity()) {
            throw new BusinessRuleException(
                    String.format("Requested quantity %d for product '%s' exceeds available stock of %d",
                            requested, product.getId(), product.getQuantity()));
        }
    }
}
