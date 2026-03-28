package com.antdevrealm.housechaosmain.product.service;

import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.cloudinary.CloudinaryService;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class InventoryServiceITest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private CloudinaryService cloudinaryService;

    @BeforeEach
    @Transactional
    void setUp() {
        cartItemRepository.deleteAll();
        orderItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // --- deductStock ---

    @Test
    void deductStock_reducesProductQuantityInDatabase() {
        ProductEntity product = savedProduct("Chair", 10);

        OrderItemEntity item = orderItem(product, 3);

        inventoryService.deductStock(List.of(item));

        ProductEntity updated = productRepository.findById(product.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(7);
    }

    @Test
    void deductStock_whenQuantityExceedsStock_throwsBusinessRuleException() {
        ProductEntity product = savedProduct("Lamp", 5);

        OrderItemEntity item = orderItem(product, 6);

        assertThrows(BusinessRuleException.class,
                () -> inventoryService.deductStock(List.of(item)));

        ProductEntity unchanged = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(5);
    }

    // --- assertSufficientStock(List) ---

    @Test
    void assertSufficientStock_list_doesNotThrowWhenStockIsSufficient() {
        ProductEntity product = savedProduct("Desk", 8);

        OrderItemEntity item = orderItem(product, 4);

        inventoryService.assertSufficientStock(List.of(item));

        ProductEntity unchanged = productRepository.findById(product.getId()).orElseThrow();
        assertThat(unchanged.getQuantity()).isEqualTo(8);
    }

    @Test
    void assertSufficientStock_list_throwsWhenAnyItemExceedsStock() {
        ProductEntity product = savedProduct("Table", 3);

        OrderItemEntity item = orderItem(product, 5);

        assertThrows(BusinessRuleException.class,
                () -> inventoryService.assertSufficientStock(List.of(item)));
    }

    // --- assertSufficientStock(ProductEntity, int) ---

    @Test
    void assertSufficientStock_singleItem_doesNotThrowWhenStockIsSufficient() {
        ProductEntity product = savedProduct("Shelf", 10);

        inventoryService.assertSufficientStock(product, 10);
    }

    @Test
    void assertSufficientStock_singleItem_throwsWhenRequestedExceedsStock() {
        ProductEntity product = savedProduct("Stool", 2);

        assertThrows(BusinessRuleException.class,
                () -> inventoryService.assertSufficientStock(product, 3));
    }

    // --- helpers ---

    private ProductEntity savedProduct(String name, int quantity) {
        CategoryEntity category = categoryRepository.save(
                CategoryEntity.builder().name(name.toLowerCase() + "-cat").build());

        return productRepository.save(ProductEntity.builder()
                .name(name)
                .description("Test description for " + name)
                .price(new BigDecimal("99.99"))
                .quantity(quantity)
                .category(category)
                .newArrival(false)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    private OrderItemEntity orderItem(ProductEntity product, int quantity) {
        return OrderItemEntity.builder()
                .product(product)
                .unitPrice(product.getPrice())
                .quantity(quantity)
                .lineTotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
    }
}
