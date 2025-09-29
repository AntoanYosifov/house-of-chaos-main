package com.antdevrealm.housechaosmain.features.order.model.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "order_items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private OrderEntity order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;
}
