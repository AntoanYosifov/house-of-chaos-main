package com.antdevrealm.housechaosmain.order;

import com.antdevrealm.housechaosmain.address.service.AddressService;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.model.entity.OrderEntity;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderRepository;
import com.antdevrealm.housechaosmain.order.service.OrderService;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CartService cartService;

    @Mock
    private AddressService addressService;

    @Mock
    private ImgUrlExpander imgUrlExpander;

    @InjectMocks
    private OrderService orderService;

    @Test
    void givenExistingOrder_whenGetById_thenOrderResponseDTOIsReturned() {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .owner(owner)
                .status(OrderStatus.NEW)
                .total(new BigDecimal("299.98"))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProductEntity product = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Chair")
                .imageUrl("/images/chair.jpg")
                .build();

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .order(order)
                .product(product)
                .unitPrice(new BigDecimal("149.99"))
                .quantity(2)
                .lineTotal(new BigDecimal("299.98"))
                .build();

        when(orderRepository.findByIdAndOwnerId(orderId, ownerId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrder(order)).thenReturn(List.of(orderItem));
        when(imgUrlExpander.toPublicUrl("/images/chair.jpg")).thenReturn("http://localhost:8080/images/chair.jpg");

        OrderResponseDTO result = orderService.getById(ownerId, orderId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(orderId);
        assertThat(result.ownerId()).isEqualTo(ownerId);
        assertThat(result.status()).isEqualTo(OrderStatus.NEW);
        assertThat(result.total()).isEqualByComparingTo(new BigDecimal("299.98"));
        assertThat(result.items()).hasSize(1);

        verify(orderRepository, times(1)).findByIdAndOwnerId(orderId, ownerId);
        verify(orderItemRepository, times(1)).findAllByOrder(order);
    }

    @Test
    void givenNonExistentOrder_whenGetById_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findByIdAndOwnerId(orderId, ownerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getById(ownerId, orderId));

        verify(orderRepository, times(1)).findByIdAndOwnerId(orderId, ownerId);
        verify(orderItemRepository, never()).findAllByOrder(any());
    }
}
