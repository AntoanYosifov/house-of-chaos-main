package com.antdevrealm.housechaosmain.features.auth.web;

import com.antdevrealm.housechaosmain.features.auth.service.AuthService;
import com.antdevrealm.housechaosmain.features.auth.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

        URI uriLocation = URI.create("/users/" + userResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(userResponseDTO);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO req, HttpServletResponse res) {
        return ResponseEntity.ok(authService.login(req, res));
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
    public String getProtected() {
        return "You got here";
    }

}
