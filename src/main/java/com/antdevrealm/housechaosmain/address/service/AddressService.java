package com.antdevrealm.housechaosmain.address.service;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.address.repository.AddressRepository;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    @Autowired
    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Transactional
    public AddressEntity create(AddressRequestDTO addressRequestDto) {
        return this.addressRepository.save(mapToEntity(addressRequestDto));
    }

    @Transactional
    public AddressEntity update(AddressRequestDTO addressRequestDTO, UUID addressId) {
        AddressEntity addressEntity = this.addressRepository.findById(addressId).
                orElseThrow(() -> new ResourceNotFoundException(String.format("Address with ID: %s not found!", addressId)));

        addressEntity.setCountry(addressRequestDTO.country());
        addressEntity.setCity(addressRequestDTO.city());
        addressEntity.setZip(addressRequestDTO.zip());
        addressEntity.setStreet(addressRequestDTO.street());
        addressEntity.setUpdatedAt(Instant.now());

        return this.addressRepository.save(addressEntity);
    }

    private AddressEntity mapToEntity(AddressRequestDTO addressRequestDTO) {
        return AddressEntity.builder()
                .country(addressRequestDTO.country())
                .city(addressRequestDTO.city())
                .zip(addressRequestDTO.zip())
                .street(addressRequestDTO.street())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
