package com.antdevrealm.housechaosmain.product.repository;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findAllByCategoryAndIsActiveIsTrue(CategoryEntity category);
    Optional<ProductEntity> findByIdAndIsActiveIsTrue(UUID id);

    boolean existsByCategory(CategoryEntity category);
}
