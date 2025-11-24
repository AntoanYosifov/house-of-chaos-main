package com.antdevrealm.housechaosmain.category.repository;

import com.antdevrealm.housechaosmain.category.model.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    Optional<CategoryEntity> findByName(String name);

    boolean existsByName(String name);
}
