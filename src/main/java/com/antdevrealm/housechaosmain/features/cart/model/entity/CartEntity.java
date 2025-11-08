package com.antdevrealm.housechaosmain.features.cart.model.entity;


import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "carts")
public class CartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(optional = false)
    private UserEntity owner;

    @OneToMany(mappedBy = "cart")
    private List<CartItemEntity> items = new ArrayList<>();
}
