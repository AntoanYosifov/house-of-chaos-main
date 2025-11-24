package com.antdevrealm.housechaosmain.product.model;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
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
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne(optional = false)
    private CategoryEntity category;

    @Column(nullable = false)
    private BigDecimal price;

    private int quantity;

    private boolean newArrival;

    private boolean isActive;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private Instant createdOn;

    @Column(nullable = false)
    private Instant updatedAt;
}
