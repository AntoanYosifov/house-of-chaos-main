package com.antdevrealm.housechaosmain.order.model.entity;


import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.order.model.enums.OrderStatus;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
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

    @ManyToOne(optional = false)
    private UserEntity owner;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createdOn;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal total;

    @ManyToOne
    private AddressEntity shippingAddress;
}
