package com.antdevrealm.housechaosmain.auth;

import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import com.antdevrealm.housechaosmain.auth.refreshtoken.repository.RefreshTokenRepository;
import com.antdevrealm.housechaosmain.auth.service.AuthService;
import com.antdevrealm.housechaosmain.order.repository.OrderItemRepository;
import com.antdevrealm.housechaosmain.order.repository.OrderRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartItemRepository;
import com.antdevrealm.housechaosmain.cart.repository.CartRepository;
import com.antdevrealm.housechaosmain.role.model.entity.RoleEntity;
import com.antdevrealm.housechaosmain.role.model.enums.UserRole;
import com.antdevrealm.housechaosmain.role.repository.RoleRepository;
import com.antdevrealm.housechaosmain.user.model.UserEntity;
import com.antdevrealm.housechaosmain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
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
    void login_authenticatesUserCreatesRefreshTokenAndReturnsAccessToken() {
        RoleEntity userRole = roleRepository.findByRole(UserRole.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));

        String email = "testuser@test.com";
        String password = "password123";
        String encodedPassword = passwordEncoder.encode(password);

        UserEntity user = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .roles(new ArrayList<>())
                .createdOn(Instant.now())
                .updatedAt(Instant.now())
                .build();
        user.getRoles().add(userRole);
        UserEntity savedUser = userRepository.save(user);

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);
        MockHttpServletResponse response = new MockHttpServletResponse();

        LoginResponseDTO loginResponse = authService.login(loginRequest, response);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.accessTokenResponseDTO()).isNotNull();
        assertThat(loginResponse.accessTokenResponseDTO().accessToken()).isNotBlank();
        assertThat(loginResponse.accessTokenResponseDTO().tokenType()).isEqualTo("Bearer");
        assertThat(loginResponse.userResponseDTO()).isNotNull();
        assertThat(loginResponse.userResponseDTO().id()).isEqualTo(savedUser.getId());
        assertThat(loginResponse.userResponseDTO().email()).isEqualTo(email);

        String setCookieHeader = response.getHeader("Set-Cookie");
        assertThat(setCookieHeader).isNotNull();
        assertThat(setCookieHeader).contains("hoc_refresh");
        assertThat(setCookieHeader).contains("HttpOnly");

        long refreshTokenCount = refreshTokenRepository.count();
        assertThat(refreshTokenCount).isEqualTo(1);
    }
}
