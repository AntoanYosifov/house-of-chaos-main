package com.antdevrealm.housechaosmain.cart.repository;

import com.antdevrealm.housechaosmain.cart.model.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
    List<CartItemEntity> findAllByCartId(UUID id);

    Optional<CartItemEntity> findByCartIdAndProductId(UUID cartId, UUID productId);
}
