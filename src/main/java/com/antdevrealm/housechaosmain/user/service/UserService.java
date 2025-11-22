package com.antdevrealm.housechaosmain.user.service;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.address.service.AddressService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.service.RoleService;
import com.antdevrealm.housechaosmain.user.dto.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UpdateProfileRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.exception.EmailAlreadyUsedException;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UserService(UserRepository userRepository,
                       RoleService roleService,
                       AddressService addressService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.addressService = addressService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseDTO register(RegistrationRequestDTO dto) {

        String normalizedEmail = dto.email().trim().toLowerCase();

        if(userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyUsedException("Email is already registered");
        }

        UserEntity newEntity = mapToEntity(dto);
        newEntity.getRoles().add(this.roleService.getByRole(UserRole.USER));
        UserEntity savedEntity = userRepository.save(newEntity);

        return ResponseDTOMapper.mapToUserResponseDTO(savedEntity);
    }

    @Transactional
    public UserResponseDTO update(UUID userId, UpdateProfileRequestDTO updateProfileRequestDTO) {
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", userId)));

        AddressRequestDTO addressRequestDTO = updateProfileRequestDTO.address();
        AddressEntity addressEntity;

        if(userEntity.getAddress() != null) {
            UUID userAddressId = userEntity.getAddress().getId();
            addressEntity = this.addressService.update(addressRequestDTO, userAddressId);
        } else {
            addressEntity = this.addressService.create(addressRequestDTO);
        }

        userEntity.setFirstName(updateProfileRequestDTO.firstName());
        userEntity.setLastName(updateProfileRequestDTO.lastName());
        userEntity.setAddress(addressEntity);
        userEntity.setUpdatedAt(Instant.now());

        return ResponseDTOMapper.mapToUserResponseDTO(this.userRepository.save(userEntity));
    }

    public UserResponseDTO getById(UUID userId) {
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", userId)));

        return ResponseDTOMapper.mapToUserResponseDTO(userEntity);
    }


    private UserEntity mapToEntity(RegistrationRequestDTO dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(this.passwordEncoder.encode(dto.password()))
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
