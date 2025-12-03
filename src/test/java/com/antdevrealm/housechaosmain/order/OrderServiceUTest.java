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
import java.util.ArrayList;
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

    @Test
    void givenNewOrdersExist_whenGetNew_thenListOfNewOrdersIsReturned() {
        UUID ownerId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        OrderEntity order1 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(OrderStatus.NEW)
                .total(new BigDecimal("100.00"))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        OrderEntity order2 = OrderEntity.builder()
                .id(UUID.randomUUID())
                .owner(owner)
                .status(OrderStatus.NEW)
                .total(new BigDecimal("200.00"))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        ProductEntity product = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Test Lamp")
                .imageUrl("/images/lamp.jpg")
                .build();

        OrderItemEntity item1 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .order(order1)
                .product(product)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(1)
                .lineTotal(new BigDecimal("100.00"))
                .build();

        OrderItemEntity item2 = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .order(order2)
                .product(product)
                .unitPrice(new BigDecimal("100.00"))
                .quantity(2)
                .lineTotal(new BigDecimal("200.00"))
                .build();

        when(orderRepository.findAllByOwnerIdAndStatus(ownerId, OrderStatus.NEW)).thenReturn(List.of(order1, order2));
        when(orderItemRepository.findAllByOrder(order1)).thenReturn(List.of(item1));
        when(orderItemRepository.findAllByOrder(order2)).thenReturn(List.of(item2));
        when(imgUrlExpander.toPublicUrl("/images/lamp.jpg")).thenReturn("http://localhost:8080/images/lamp.jpg");

        List<OrderResponseDTO> result = orderService.getNew(ownerId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.NEW);
        assertThat(result.get(1).status()).isEqualTo(OrderStatus.NEW);

        verify(orderRepository, times(1)).findAllByOwnerIdAndStatus(ownerId, OrderStatus.NEW);
        verify(orderItemRepository, times(1)).findAllByOrder(order1);
        verify(orderItemRepository, times(1)).findAllByOrder(order2);
    }

    @Test
    void givenNoNewOrders_whenGetNew_thenEmptyListIsReturned() {
        UUID ownerId = UUID.randomUUID();

        when(orderRepository.findAllByOwnerIdAndStatus(ownerId, OrderStatus.NEW)).thenReturn(new ArrayList<>());

        List<OrderResponseDTO> result = orderService.getNew(ownerId);

        assertThat(result).isEmpty();

        verify(orderRepository, times(1)).findAllByOwnerIdAndStatus(ownerId, OrderStatus.NEW);
        verify(orderItemRepository, never()).findAllByOrder(any());
    }

    @Test
    void givenExistingOrder_whenDelete_thenOrderAndItemsAreDeleted() {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        UserEntity owner = UserEntity.builder()
                .id(ownerId)
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(orderId)
                .owner(owner)
                .status(OrderStatus.NEW)
                .total(new BigDecimal("149.99"))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(orderRepository.findByIdAndOwnerId(orderId, ownerId)).thenReturn(Optional.of(order));

        orderService.delete(ownerId, orderId);

        verify(orderRepository, times(1)).findByIdAndOwnerId(orderId, ownerId);
        verify(orderItemRepository, times(1)).deleteAllByOrder(order);
        verify(orderRepository, times(1)).delete(order);
    }

    @Test
    void givenNonExistentOrder_whenDelete_thenResourceNotFoundExceptionIsThrown() {
        UUID ownerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findByIdAndOwnerId(orderId, ownerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.delete(ownerId, orderId));

        verify(orderRepository, times(1)).findByIdAndOwnerId(orderId, ownerId);
        verify(orderItemRepository, never()).deleteAllByOrder(any());
        verify(orderRepository, never()).delete(any());
    }
}
