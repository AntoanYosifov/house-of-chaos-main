package com.antdevrealm.housechaosmain.auth;

import com.antdevrealm.housechaosmain.auth.dto.token.IssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.token.LoginIssuedTokenDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.repository.RefreshTokenRepository;
import com.antdevrealm.housechaosmain.auth.service.AuthService;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderRepository;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuthServiceITest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void login_returnsIssuedTokenAndUserDTO_andPersistsRefreshToken() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        String email = "testuser@test.com";
        String password = "password123";

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        UserEntity savedUser = userRepository.save(user);

        LoginIssuedTokenDTO result = authService.login(new LoginRequestDTO(email, password));

        assertThat(result).isNotNull();
        assertThat(result.issuedToken().accessToken()).isNotBlank();
        assertThat(result.issuedToken().rawRefreshToken()).isNotBlank();
        assertThat(result.issuedToken().refreshExpiresAt()).isAfter(Instant.now());
        assertThat(result.issuedToken().accessTtlSeconds()).isPositive();
        assertThat(result.user().id()).isEqualTo(savedUser.getId());
        assertThat(result.user().email()).isEqualTo(email);

        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void refresh_rotatesToken_andReturnsNewAccessToken() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        String email = "refresh@test.com";
        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        userRepository.save(user);

        LoginIssuedTokenDTO loginResult = authService.login(new LoginRequestDTO(email, "password123"));
        String rawRefreshToken = loginResult.issuedToken().rawRefreshToken();

        IssuedTokenDTO refreshResult = authService.refresh(rawRefreshToken);

        assertThat(refreshResult.accessToken()).isNotBlank();
        assertThat(refreshResult.rawRefreshToken()).isNotBlank();
        assertThat(refreshResult.rawRefreshToken()).isNotEqualTo(rawRefreshToken);
        assertThat(refreshResult.refreshExpiresAt()).isAfter(Instant.now());
        assertThat(refreshTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void logout_deletesRefreshToken() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        String email = "logout@test.com";
        UserEntity user = UserEntity.builder()
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        userRepository.save(user);

        LoginIssuedTokenDTO loginResult = authService.login(new LoginRequestDTO(email, "password123"));
        String rawRefreshToken = loginResult.issuedToken().rawRefreshToken();

        assertThat(refreshTokenRepository.count()).isEqualTo(1);

        authService.logout(rawRefreshToken);

        assertThat(refreshTokenRepository.count()).isEqualTo(0);
    }
}
