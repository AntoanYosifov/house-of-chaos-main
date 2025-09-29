package com.antdevrealm.housechaosmain.features.user.repository;

import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {}
