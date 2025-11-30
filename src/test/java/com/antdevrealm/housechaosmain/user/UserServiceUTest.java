package com.antdevrealm.housechaosmain.user;

import com.antdevrealm.housechaosmain.cart.service.CartService;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.service.RoleService;
import com.antdevrealm.housechaosmain.user.dto.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import com.antdevrealm.housechaosmain.user.exception.EmailAlreadyUsedException;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import com.antdevrealm.housechaosmain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceUTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleService roleService;

    @Mock
    private CartService cartService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void givenUniqueEmail_whenRegister_thenUserIsRegisteredAndCartIsCreated() {
        String uniqueEmail = "test@example.com";
        String normalizedEmail = uniqueEmail.trim().toLowerCase();
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO(uniqueEmail, password, password);

        RoleEntity userRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.USER)
                .build();

        UUID generatedUserId = UUID.randomUUID();
        UserEntity savedEntity = UserEntity.builder()
                .id(generatedUserId)
                .email(uniqueEmail)
                .password(encodedPassword)
                .roles(new java.util.ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        savedEntity.getRoles().add(userRole);

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(false);
        when(roleService.getByRole(UserRole.USER)).thenReturn(userRole);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedEntity);

        UserResponseDTO result = userService.register(requestDTO);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository, times(1)).save(captor.capture());

        UserEntity capturedEntity = captor.getValue();
        assertThat(capturedEntity.getRoles()).contains(userRole);
        assertThat(capturedEntity.getEmail()).isEqualTo(uniqueEmail);
        assertThat(capturedEntity.getPassword()).isEqualTo(encodedPassword);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(generatedUserId);
        assertThat(result.email()).isEqualTo(uniqueEmail);
        assertThat(result.roles()).contains(UserRole.USER);

        verify(userRepository, times(1)).existsByEmail(normalizedEmail);
        verify(roleService, times(1)).getByRole(UserRole.USER);
        verify(passwordEncoder, times(1)).encode(password);
        verify(cartService, times(1)).createCart(savedEntity);
    }

    @Test
    void givenExistingEmail_whenRegister_thenEmailAlreadyUsedExceptionIsThrown() {
        String existingEmail = "existing@example.com";
        String normalizedEmail = existingEmail.trim().toLowerCase();
        String password = "password123";

        RegistrationRequestDTO requestDTO = new RegistrationRequestDTO(existingEmail, password, password);

        when(userRepository.existsByEmail(normalizedEmail)).thenReturn(true);

        assertThrows(EmailAlreadyUsedException.class, () -> userService.register(requestDTO));

        verify(userRepository, times(1)).existsByEmail(normalizedEmail);
        verify(roleService, never()).getByRole(any(UserRole.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(cartService, never()).createCart(any(UserEntity.class));
    }
}

