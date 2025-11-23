package com.antdevrealm.housechaosmain.cart.repository;

import com.antdevrealm.housechaosmain.cart.model.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, UUID> {
}
