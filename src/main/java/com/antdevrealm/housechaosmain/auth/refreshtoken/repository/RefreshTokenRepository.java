package com.antdevrealm.housechaosmain.auth.refreshtoken.repository;

import com.antdevrealm.housechaosmain.auth.refreshtoken.model.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
    long deleteByUserId (UUID userId);
    long deleteByTokenHash (String tokenHash);
}
