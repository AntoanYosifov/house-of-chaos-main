package com.antdevrealm.housechaosmain.address.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Table(name = "addresses")
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    String country;

    @Column(nullable = false)
    String city;

    @Column(nullable = false)
    int zip;

    @Column(nullable = false)
    String street;

    @Column(nullable = false)
    Instant createdOn;

    @Column(nullable = false)
    Instant updatedAt;
}
