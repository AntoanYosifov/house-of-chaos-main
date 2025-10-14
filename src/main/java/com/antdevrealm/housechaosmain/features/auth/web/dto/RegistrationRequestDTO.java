package com.antdevrealm.housechaosmain.features.auth.web.dto;

import com.antdevrealm.housechaosmain.features.auth.web.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@PasswordMatch(first = "password", second = "confirmPassword", message = "Passwords must match")
public record RegistrationRequestDTO(@NotBlank @Email String email,
                                     @NotBlank @Size(min = 5, max = 20) String password,
                                     @NotBlank @Size(min = 5, max = 20) String confirmPassword) {

}
