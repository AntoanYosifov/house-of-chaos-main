package com.antdevrealm.housechaosmain.user.dto;

import com.antdevrealm.housechaosmain.auth.web.validation.PasswordMatch;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@PasswordMatch(first = "password", second = "confirmPassword", message = "Passwords must match")
public record RegistrationRequestDTO(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 5, max = 20, message = "Password must be between {min} and {max} characters")
        String password,

        @NotBlank(message = "Confirm password is required")
        @Size(min = 5, max = 20, message = "Confirm password must be between {min} and {max} characters")
        String confirmPassword) {

}
