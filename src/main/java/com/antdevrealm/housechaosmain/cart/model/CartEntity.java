package com.antdevrealm.housechaosmain.cart.model;


import com.antdevrealm.housechaosmain.user.model.UserEntity;
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

    @OneToMany(mappedBy = "cart", fetch = FetchType.EAGER)
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();
}
