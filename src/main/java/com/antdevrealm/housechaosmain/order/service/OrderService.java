package com.antdevrealm.housechaosmain.order.service;

import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.exception.BusinessRuleException;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.order.dto.CreateOrderItemRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.CreateOrderRequestDTO;
import com.antdevrealm.housechaosmain.order.dto.OrderItemResponseDTO;
import com.antdevrealm.housechaosmain.order.dto.OrderResponseDTO;
import com.antdevrealm.housechaosmain.order.model.entity.OrderEntity;
import com.antdevrealm.housechaosmain.order.model.entity.OrderItemEntity;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderRepository;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import com.antdevrealm.housechaosmain.product.repository.ProductRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ImgUrlExpander;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final CartService cartService;

    private final ImgUrlExpander imgUrlExpander;


    @Autowired
    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository, UserRepository userRepository, ProductRepository productRepository, CartService cartService, ImgUrlExpander imgUrlExpander) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.imgUrlExpander = imgUrlExpander;
    }

    @Transactional
    public OrderResponseDTO create(UUID ownerId, CreateOrderRequestDTO orderRequestDTO) {
        UserEntity userEntity = this.userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", ownerId)));

        List<CreateOrderItemRequestDTO> createOrderItemRequestDTOS = orderRequestDTO.items();
        List<OrderItemEntity> orderItemEntities = createOrderItemRequestDTOS.stream().map(this::mapToItemEntity).toList();

        BigDecimal orderTotal = calculateOrderTotal(orderItemEntities);

        OrderEntity orderEntity = OrderEntity.builder()
                .owner(userEntity)
                .status(OrderStatus.NEW)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .total(orderTotal)
                .build();

        OrderEntity savedOrder = this.orderRepository.save(orderEntity);
        orderItemEntities.forEach(item -> item.setOrder(savedOrder));

        List<OrderItemEntity> savedItems = this.orderItemRepository.saveAll(orderItemEntities);

        this.cartService.clearCartItems(userEntity);

        return mapToOrderResponseDto(savedOrder, savedItems);
    }

    public OrderResponseDTO getById(UUID ownerId, UUID id) {
        UserEntity userEntity = this.userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", ownerId)));

        OrderEntity orderEntity = this.orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Order with ID: %s not found!", id)));

        if(!orderEntity.getOwner().getId().equals(userEntity.getId())) {
            throw new BusinessRuleException(String.format("Order with ID: %s does not belong to user with ID: %s ", id, ownerId));
        }

        List<OrderItemEntity> items = this.orderItemRepository.findAllByOrder(orderEntity);

        return mapToOrderResponseDto(orderEntity, items);

    }

    private OrderResponseDTO mapToOrderResponseDto(OrderEntity orderEntity, List<OrderItemEntity> items) {
        List<OrderItemResponseDTO> itemDTOs = items.stream()
                .map(this::mapToItemResponseDto).toList();

        return new OrderResponseDTO(orderEntity.getId(),
                orderEntity.getOwner().getId(),
                orderEntity.getStatus(),
                orderEntity.getCreatedOn(),
                orderEntity.getUpdatedAt(),
                orderEntity.getTotal(),
                ResponseDTOMapper.mapToAddressResponseDTO(orderEntity.getShippingAddress()),
                itemDTOs);
    }

    private OrderItemResponseDTO mapToItemResponseDto(OrderItemEntity orderItemEntity) {
        return new OrderItemResponseDTO(orderItemEntity.getId(),
                orderItemEntity.getProduct().getId(),
                orderItemEntity.getProduct().getName(),
                orderItemEntity.getUnitPrice(),
                this.imgUrlExpander.toPublicUrl(orderItemEntity.getProduct().getImageUrl()),
                orderItemEntity.getQuantity(),
                orderItemEntity.getLineTotal());
    }

    private OrderItemEntity mapToItemEntity(CreateOrderItemRequestDTO dto) {
        ProductEntity productEntity = this.productRepository.findByIdAndIsActiveIsTrue(dto.productId())
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Product with ID: %s not found!", dto.productId())));

        if(dto.quantity() > productEntity.getQuantity()) {
            throw new BusinessRuleException(String.format("Order item quantity: %d can not exceed product available quantity in stock: %d", dto.quantity(), productEntity.getQuantity()));
        }

        return OrderItemEntity.builder()
                .product(productEntity)
                .unitPrice(productEntity.getPrice())
                .quantity(dto.quantity())
                .lineTotal(calculateLineTotal(productEntity.getPrice(), dto.quantity()))
                .build();
    }

    private BigDecimal calculateLineTotal(BigDecimal unitPrice, int quantity) {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal calculateOrderTotal(List<OrderItemEntity> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

           return items.stream()
                    .map(OrderItemEntity::getLineTotal)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


}
