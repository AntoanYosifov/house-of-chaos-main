package com.antdevrealm.housechaosmain.order.model.entity;

import com.antdevrealm.housechaosmain.product.model.ProductEntity;
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

    @ManyToOne(optional = false)
    private ProductEntity product;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    private int quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal lineTotal;
}
