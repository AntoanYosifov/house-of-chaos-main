package com.antdevrealm.housechaosmain.user.service;

import com.antdevrealm.housechaosmain.address.dto.AddressRequestDTO;
import com.antdevrealm.housechaosmain.address.model.AddressEntity;
import com.antdevrealm.housechaosmain.address.service.AddressService;
import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.service.RoleService;
import com.antdevrealm.housechaosmain.user.dto.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UpdateProfileRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.exception.EmailAlreadyUsedException;
import com.antdevrealm.housechaosmain.user.exception.UserAlreadyHasRoleException;
import com.antdevrealm.housechaosmain.user.exception.UserHasNoRoleException;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.util.ResponseDTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final CartService cartService;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleService roleService, CartService cartService,
                       AddressService addressService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.cartService = cartService;
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

        this.cartService.createCart(savedEntity);

        log.info("User registered: id={}, email={}", savedEntity.getId(), normalizedEmail);
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

        UserEntity saved = this.userRepository.save(userEntity);

        log.info("User profile updated: id={}",
                saved.getId());

        return ResponseDTOMapper.mapToUserResponseDTO(saved);
    }

    public UserResponseDTO getById(UUID userId) {
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", userId)));

        return ResponseDTOMapper.mapToUserResponseDTO(userEntity);
    }

    public List<UserResponseDTO> getAll() {
        return this.userRepository.findAll().stream()
                .map(ResponseDTOMapper::mapToUserResponseDTO).toList();
    }

    @Transactional
    public UserResponseDTO addAdminRole(UUID userId) {
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", userId)));

        RoleEntity adminRole = this.roleService.getByRole(UserRole.ADMIN);
        List<RoleEntity> userRoles = userEntity.getRoles();
        if(userRoles.contains(adminRole)) {
            throw new UserAlreadyHasRoleException("User already has role: " + adminRole.getRole().toString());
        }

        userRoles.add(adminRole);
        UserEntity saved = this.userRepository.save(userEntity);
        log.info("Admin role granted: userId={}, role={}", saved.getId(), adminRole.getRole());
        return ResponseDTOMapper.mapToUserResponseDTO(saved);
    }

    @Transactional
    public UserResponseDTO removeAdminRole(UUID userId) {
        UserEntity userEntity = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("User with ID: %s not found!", userId)));

        RoleEntity adminRole = this.roleService.getByRole(UserRole.ADMIN);
        List<RoleEntity> userRoles = userEntity.getRoles();
        if(!userRoles.contains(adminRole)) {
            throw new UserHasNoRoleException("User doesn't have a role: " + adminRole.getRole().toString());
        }

        userRoles.remove(adminRole);

        UserEntity saved = this.userRepository.save(userEntity);

        log.info("Admin role removed: userId={}, role={}", saved.getId(), adminRole.getRole());
        return ResponseDTOMapper.mapToUserResponseDTO(saved);
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
