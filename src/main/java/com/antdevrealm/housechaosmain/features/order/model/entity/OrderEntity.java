package com.antdevrealm.housechaosmain.features.order.model.entity;


import com.antdevrealm.housechaosmain.features.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.features.user.model.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private UserEntity owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "order")
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();
}
