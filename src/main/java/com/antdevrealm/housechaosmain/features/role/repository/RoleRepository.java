package com.antdevrealm.housechaosmain.features.role.repository;

import com.antdevrealm.housechaosmain.features.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.features.role.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    Optional<RoleEntity> findByRole(UserRole role);
}
