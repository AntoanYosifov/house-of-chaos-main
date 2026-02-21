package com.antdevrealm.housechaosmain.product.repository;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import com.antdevrealm.housechaosmain.product.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {

    Page<ProductEntity> findAllByIsActiveIsTrue(Pageable pageable);

    Page<ProductEntity> findAllByCategoryAndIsActiveIsTrue(CategoryEntity category, Pageable pageable);

    Optional<ProductEntity> findByIdAndIsActiveIsTrue(UUID id);

    @Query(
            value = """
                    SELECT * FROM house_of_chaos_main.products p
                    WHERE p.new_arrival = true
                    AND p.is_active = true
                    LIMIT 10
                    """,
            nativeQuery = true
    )
    List<ProductEntity> findTop10NewArrivals();

    @Query(
            value = """
                    SELECT * FROM house_of_chaos_main.products p
                    WHERE p.is_active = true
                    ORDER BY p.price
                    LIMIT 10
                    """,
            nativeQuery = true
    )
    List<ProductEntity> findTop10Cheapest();

    @Modifying
    @Query(
            """
            UPDATE ProductEntity p
            SET p.newArrival = false
            WHERE p.newArrival = true
            AND p.isActive = true
            AND p.createdOn < :threshold
            """
    )
    int markOldNewArrivalsAsNotNew(Instant threshold);

    boolean existsByCategory(CategoryEntity category);
}
