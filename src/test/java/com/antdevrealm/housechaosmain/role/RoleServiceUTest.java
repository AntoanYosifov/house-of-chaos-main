package com.antdevrealm.housechaosmain.role;

import com.antdevrealm.housechaosmain.exception.ResourceNotFoundException;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.role.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RoleServiceUTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @Test
    void givenExistingUserRole_whenGetByRole_thenRoleEntityIsReturned() {
        UserRole userRole = UserRole.USER;

        RoleEntity roleEntity = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(userRole)
                .build();

        when(roleRepository.findByRole(userRole)).thenReturn(Optional.of(roleEntity));

        RoleEntity result = roleService.getByRole(userRole);

        assertThat(result).isEqualTo(roleEntity);
        assertThat(result.getRole()).isEqualTo(userRole);

        verify(roleRepository, times(1)).findByRole(userRole);
    }

    @Test
    void givenNonExistentUserRole_whenGetByRole_thenResourceNotFoundExceptionIsThrown() {
        UserRole userRole = UserRole.ADMIN;

        when(roleRepository.findByRole(userRole)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> roleService.getByRole(userRole));

        verify(roleRepository, times(1)).findByRole(userRole);
    }
}

