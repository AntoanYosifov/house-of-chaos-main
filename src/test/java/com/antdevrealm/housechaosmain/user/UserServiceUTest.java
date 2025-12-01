package com.antdevrealm.housechaosmain.user;

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
import java.util.ArrayList;
import java.util.Optional;
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
    private AddressService addressService;

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
                .roles(new ArrayList<>())
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

    @Test
    void givenExistingUserWithAddress_whenUpdate_thenUserAndAddressAreUpdated() {
        UUID userId = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        String country = "Bulgaria";
        String city = "Sofia";
        Integer zip = 1000;
        String street = "Main Street 1";

        AddressRequestDTO addressRequestDTO = new AddressRequestDTO(country, city, zip, street);
        UpdateProfileRequestDTO updateRequestDTO = new UpdateProfileRequestDTO(firstName, lastName, addressRequestDTO);

        UUID existingAddressId = UUID.randomUUID();
        AddressEntity existingAddress = AddressEntity.builder()
                .id(existingAddressId)
                .country("Old Country")
                .city("Old City")
                .zip(2000)
                .street("Old Street")
                .createdOn(Instant.now().minusSeconds(3600))
                .updatedAt(Instant.now().minusSeconds(3600))
                .build();

        UserEntity existingUser = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Old First")
                .lastName("Old Last")
                .address(existingAddress)
                .roles(new ArrayList<>())
                .createdOn(Instant.now().minusSeconds(7200))
                .updatedAt(Instant.now().minusSeconds(7200))
                .build();

        AddressEntity updatedAddress = AddressEntity.builder()
                .id(existingAddressId)
                .country(country)
                .city(city)
                .zip(zip)
                .street(street)
                .createdOn(existingAddress.getCreatedOn())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(addressService.update(addressRequestDTO, existingAddressId)).thenReturn(updatedAddress);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDTO result = userService.update(userId, updateRequestDTO);

        assertThat(existingUser.getFirstName()).isEqualTo(firstName);
        assertThat(existingUser.getLastName()).isEqualTo(lastName);
        assertThat(existingUser.getAddress()).isEqualTo(updatedAddress);
        assertThat(existingUser.getUpdatedAt()).isNotNull();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(addressService, times(1)).update(addressRequestDTO, existingAddressId);
        verify(addressService, never()).create(any(AddressRequestDTO.class));
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void givenExistingUserWithoutAddress_whenUpdate_thenUserIsUpdatedAndAddressIsCreated() {
        UUID userId = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        String country = "Bulgaria";
        String city = "Sofia";
        Integer zip = 1000;
        String street = "Main Street 1";

        AddressRequestDTO addressRequestDTO = new AddressRequestDTO(country, city, zip, street);
        UpdateProfileRequestDTO updateRequestDTO = new UpdateProfileRequestDTO(firstName, lastName, addressRequestDTO);

        UserEntity existingUser = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Old First")
                .lastName("Old Last")
                .address(null)
                .roles(new ArrayList<>())
                .createdOn(Instant.now().minusSeconds(7200))
                .updatedAt(Instant.now().minusSeconds(7200))
                .build();

        AddressEntity newAddress = AddressEntity.builder()
                .id(UUID.randomUUID())
                .country(country)
                .city(city)
                .zip(zip)
                .street(street)
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(addressService.create(addressRequestDTO)).thenReturn(newAddress);
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserResponseDTO result = userService.update(userId, updateRequestDTO);

        assertThat(existingUser.getFirstName()).isEqualTo(firstName);
        assertThat(existingUser.getLastName()).isEqualTo(lastName);
        assertThat(existingUser.getAddress()).isEqualTo(newAddress);
        assertThat(existingUser.getUpdatedAt()).isNotNull();

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(addressService, times(1)).create(addressRequestDTO);
        verify(addressService, never()).update(any(AddressRequestDTO.class), any(UUID.class));
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void givenNonExistentUserId_whenUpdate_thenResourceNotFoundExceptionIsThrown() {
        UUID userId = UUID.randomUUID();
        String firstName = "John";
        String lastName = "Doe";
        AddressRequestDTO addressRequestDTO = new AddressRequestDTO("Bulgaria", "Sofia", 1000, "Main Street 1");
        UpdateProfileRequestDTO updateRequestDTO = new UpdateProfileRequestDTO(firstName, lastName, addressRequestDTO);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.update(userId, updateRequestDTO));

        verify(userRepository, times(1)).findById(userId);
        verify(addressService, never()).create(any(AddressRequestDTO.class));
        verify(addressService, never()).update(any(AddressRequestDTO.class), any(UUID.class));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void givenExistingUserId_whenGetById_thenUserResponseDTOIsReturned() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        RoleEntity userRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.USER)
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .email(email)
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userEntity.getRoles().add(userRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserResponseDTO result = userService.getById(userId);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.roles()).contains(UserRole.USER);

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenNonExistentUserId_whenGetById_thenResourceNotFoundExceptionIsThrown() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getById(userId));

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void givenExistingUserWithoutAdminRole_whenAddAdminRole_thenAdminRoleIsAdded() {
        UUID userId = UUID.randomUUID();

        RoleEntity userRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.USER)
                .build();

        RoleEntity adminRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userEntity.getRoles().add(userRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(roleService.getByRole(UserRole.ADMIN)).thenReturn(adminRole);
        when(userRepository.save(userEntity)).thenReturn(userEntity);

        UserResponseDTO result = userService.addAdminRole(userId);

        assertThat(userEntity.getRoles()).contains(adminRole);
        assertThat(userEntity.getRoles()).hasSize(2);
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.roles()).contains(UserRole.USER, UserRole.ADMIN);

        verify(userRepository, times(1)).findById(userId);
        verify(roleService, times(1)).getByRole(UserRole.ADMIN);
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    void givenNonExistentUserId_whenAddAdminRole_thenResourceNotFoundExceptionIsThrown() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.addAdminRole(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(roleService, never()).getByRole(any(UserRole.class));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void givenUserAlreadyHasAdminRole_whenAddAdminRole_thenUserAlreadyHasRoleExceptionIsThrown() {
        UUID userId = UUID.randomUUID();

        RoleEntity userRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.USER)
                .build();

        RoleEntity adminRole = RoleEntity.builder()
                .id(UUID.randomUUID())
                .role(UserRole.ADMIN)
                .build();

        UserEntity userEntity = UserEntity.builder()
                .id(userId)
                .email("test@example.com")
                .password("encodedPassword")
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        userEntity.getRoles().add(userRole);
        userEntity.getRoles().add(adminRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(roleService.getByRole(UserRole.ADMIN)).thenReturn(adminRole);

        assertThrows(UserAlreadyHasRoleException.class, () -> userService.addAdminRole(userId));

        verify(userRepository, times(1)).findById(userId);
        verify(roleService, times(1)).getByRole(UserRole.ADMIN);
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}

