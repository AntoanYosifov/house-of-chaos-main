package com.antdevrealm.housechaosmain.auth.web;

import com.antdevrealm.housechaosmain.auth.dto.accesstoken.AccessTokenResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginRequestDTO;
import com.antdevrealm.housechaosmain.auth.dto.login.LoginResponseDTO;
import com.antdevrealm.housechaosmain.auth.dto.registration.RegistrationRequestDTO;
import com.antdevrealm.housechaosmain.auth.model.HOCUserDetails;
import com.antdevrealm.housechaosmain.auth.service.AuthService;
import com.antdevrealm.housechaosmain.user.dto.UserResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegistrationRequestDTO req) {
        UserResponseDTO userResponseDTO = authService.register(req);

        URI uriLocation = URI.create("/api/users/" + userResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(userResponseDTO);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO req, HttpServletResponse res) {
        LoginResponseDTO loginResponseDTO = authService.login(req, res);
        return ResponseEntity.ok(loginResponseDTO);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req, HttpServletResponse res) {
        this.authService.logout(req, res);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AccessTokenResponseDTO> refresh(HttpServletRequest req, HttpServletResponse res) {
        AccessTokenResponseDTO accessTokenResponseDTO = authService.refreshToken(req, res);
        return ResponseEntity.ok(accessTokenResponseDTO);
    }
    // mock protected endpoint for testing purposes
    @GetMapping("/protected")
    public String getProtected(@AuthenticationPrincipal HOCUserDetails principal) {
        return "You got here";
    }

}
