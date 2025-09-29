package com.antdevrealm.housechaosmain.features.cart.model.entity;


import com.antdevrealm.housechaosmain.features.product.model.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private CartEntity cart;

    @ManyToOne(optional = false)
    private ProductEntity product;

    private int quantity;
}
