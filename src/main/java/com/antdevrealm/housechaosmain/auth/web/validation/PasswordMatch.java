package com.antdevrealm.housechaosmain.auth.web.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
public @interface PasswordMatch {
    String message() default "{password.match}";

    String first();
    String second();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
