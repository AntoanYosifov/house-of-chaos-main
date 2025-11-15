package com.antdevrealm.housechaosmain.cart.model;


import com.antdevrealm.housechaosmain.product.model.ProductEntity;
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
public class CartItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private CartEntity cart;

    @ManyToOne(optional = false)
    private ProductEntity product;

    private int quantity;
}
