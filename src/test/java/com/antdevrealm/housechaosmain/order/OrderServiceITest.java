package com.antdevrealm.housechaosmain.order;

import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.category.repository.CategoryRepository;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.order.dto.CreateOrderItemRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.CreateOrderRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.model.entity.OrderEntity;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderRepository;
import com.antdevrealm.housechaosmain.order.service.OrderService;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderServiceITest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void create_createsOrderAndClearsCart() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        UserEntity user = UserEntity.builder()
                .email("testuser@test.com")
                .password("encodedPassword")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        UserEntity savedUser = userRepository.save(user);

        CartEntity cart = CartEntity.builder()
                .owner(savedUser)
                .build();
        CartEntity savedCart = cartRepository.save(cart);

        CategoryEntity category = CategoryEntity.builder()
                .name("chair")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Chair")
                .description("Test description for chair")
                .price(new BigDecimal("149.99"))
                .quantity(10)
                .imageUrl("http://example.com/chair.jpg")
                .category(savedCategory)
                .newArrival(true)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ProductEntity savedProduct = productRepository.save(product);

        CartItemEntity cartItem = CartItemEntity.builder()
                .cart(savedCart)
                .product(savedProduct)
                .quantity(2)
                .build();
        cartItemRepository.save(cartItem);

        CreateOrderItemRequestDTO orderItemRequest = new CreateOrderItemRequestDTO(savedProduct.getId(), 2);
        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO(List.of(orderItemRequest));

        OrderResponseDTO orderResponse = orderService.create(savedUser.getId(), orderRequest);

        Optional<OrderEntity> savedOrder = orderRepository.findById(orderResponse.id());
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(savedOrder.get().getTotal()).isEqualByComparingTo(new BigDecimal("299.98"));
        assertThat(savedOrder.get().getOwner().getId()).isEqualTo(savedUser.getId());

        List<OrderItemEntity> orderItems = orderItemRepository.findAllByOrder(savedOrder.get());
        assertThat(orderItems).hasSize(1);
        assertThat(orderItems.get(0).getProduct().getId()).isEqualTo(savedProduct.getId());
        assertThat(orderItems.get(0).getQuantity()).isEqualTo(2);

        List<CartItemEntity> cartItems = cartItemRepository.findAllByCart(savedCart);
        assertThat(cartItems).isEmpty();
    }

    @Test
    void create_whenUserNotFound_thenResourceNotFoundExceptionIsThrown() {
        UUID nonExistentUserId = UUID.randomUUID();

        CreateOrderItemRequestDTO orderItemRequest = new CreateOrderItemRequestDTO(UUID.randomUUID(), 1);
        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO(List.of(orderItemRequest));

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(nonExistentUserId, orderRequest));
    }

    @Test
    void create_whenProductNotFound_thenResourceNotFoundExceptionIsThrown() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        UserEntity user = UserEntity.builder()
                .email("testuser@test.com")
                .password("encodedPassword")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        UserEntity savedUser = userRepository.save(user);

        UUID nonExistentProductId = UUID.randomUUID();
        CreateOrderItemRequestDTO orderItemRequest = new CreateOrderItemRequestDTO(nonExistentProductId, 1);
        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO(List.of(orderItemRequest));

        assertThrows(ResourceNotFoundException.class, () -> orderService.create(savedUser.getId(), orderRequest));
    }

    @Test
    void create_whenQuantityExceedsProductQuantity_thenBusinessRuleExceptionIsThrown() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        UserEntity user = UserEntity.builder()
                .email("testuser@test.com")
                .password("encodedPassword")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        UserEntity savedUser = userRepository.save(user);

        CategoryEntity category = CategoryEntity.builder()
                .name("lamp")
                .build();
        CategoryEntity savedCategory = categoryRepository.save(category);

        ProductEntity product = ProductEntity.builder()
                .name("Test Lamp")
                .description("Test description for lamp")
                .price(new BigDecimal("99.99"))
                .quantity(5)
                .imageUrl("http://example.com/lamp.jpg")
                .category(savedCategory)
                .newArrival(true)
                .isActive(true)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ProductEntity savedProduct = productRepository.save(product);

        CreateOrderItemRequestDTO orderItemRequest = new CreateOrderItemRequestDTO(savedProduct.getId(), 10);
        CreateOrderRequestDTO orderRequest = new CreateOrderRequestDTO(List.of(orderItemRequest));

        assertThrows(BusinessRuleException.class, () -> orderService.create(savedUser.getId(), orderRequest));
    }
}
