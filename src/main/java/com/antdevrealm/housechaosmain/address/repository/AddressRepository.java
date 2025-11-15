package com.antdevrealm.housechaosmain.address.repository;

import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, UUID> {
}
