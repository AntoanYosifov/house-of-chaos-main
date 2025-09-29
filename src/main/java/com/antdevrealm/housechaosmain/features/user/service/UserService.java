package com.antdevrealm.housechaosmain.features.user.service;

import com.antdevrealm.housechaosmain.features.user.model.dto.RegistrationDTO;
import com.antdevrealm.housechaosmain.features.user.model.dto.RegistrationResponseDTO;
import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import com.antdevrealm.housechaosmain.features.user.model.enums.UserRole;
import com.antdevrealm.housechaosmain.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegistrationResponseDTO register(RegistrationDTO dto) {

        UserEntity newEntity = mapToEntity(dto);
        UserEntity savedEntity = userRepository.save(newEntity);

        return mapToResponseDto(savedEntity);
    }

    private static RegistrationResponseDTO mapToResponseDto(UserEntity savedEntity) {
        return new RegistrationResponseDTO(savedEntity.getId(),
                savedEntity.getEmail(),
                savedEntity.isActive(),
                savedEntity.getCreatedOn(),
                savedEntity.getUpdatedAt());
    }

    private static UserEntity mapToEntity(RegistrationDTO dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(dto.password())
                .role(UserRole.USER)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

}
