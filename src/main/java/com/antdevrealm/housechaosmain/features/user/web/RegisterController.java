package com.antdevrealm.housechaosmain.features.user.web;

import com.antdevrealm.housechaosmain.features.user.model.dto.RegistrationDTO;
import com.antdevrealm.housechaosmain.features.user.model.dto.RegistrationResponseDTO;
import com.antdevrealm.housechaosmain.features.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class RegisterController {

    private final UserService userService;

    @Autowired
    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDTO> register(@RequestBody RegistrationDTO dto) {
        RegistrationResponseDTO registrationResponseDTO = userService.register(dto);

        URI uriLocation = URI.create("/users/" + registrationResponseDTO.id());

        return ResponseEntity.created(uriLocation).body(registrationResponseDTO);
    }



}
