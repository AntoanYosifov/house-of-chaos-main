package com.antdevrealm.housechaosmain.features.auth.service;

import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationRequest;
import com.antdevrealm.housechaosmain.features.auth.web.dto.RegistrationResponse;
import com.antdevrealm.housechaosmain.features.user.model.entity.UserEntity;
import com.antdevrealm.housechaosmain.features.user.model.enums.UserRole;
import com.antdevrealm.housechaosmain.features.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService (UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public RegistrationResponse register(RegistrationRequest dto) {

        UserEntity newEntity = mapToEntity(dto);
        UserEntity savedEntity = userRepository.save(newEntity);

        return mapToResponseDto(savedEntity);
    }

    private static RegistrationResponse mapToResponseDto(UserEntity savedEntity) {
        return new RegistrationResponse(savedEntity.getId(),
                savedEntity.getEmail(),
                savedEntity.isActive(),
                savedEntity.getCreatedOn(),
                savedEntity.getUpdatedAt());
    }

    private UserEntity mapToEntity(RegistrationRequest dto) {
        return UserEntity.builder()
                .email(dto.email())
                .password(this.passwordEncoder.encode(dto.password()))
                .role(UserRole.USER)
                .active(true)
                .createdOn(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
