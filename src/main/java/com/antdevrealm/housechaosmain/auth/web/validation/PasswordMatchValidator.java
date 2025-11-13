package com.antdevrealm.housechaosmain.auth.web.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String first;
    private String second;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        this.first = constraintAnnotation.first();
        this.second = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object objectValue, ConstraintValidatorContext constraintValidatorContext) {
        if (objectValue == null) return true;

        Object firstVal = new BeanWrapperImpl(objectValue).getPropertyValue(first);
        Object secondVal = new BeanWrapperImpl(objectValue).getPropertyValue(second);

        boolean matches = (firstVal == null && secondVal == null)
                || (firstVal != null && firstVal.equals(secondVal));

        if (!matches) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(second)
                    .addConstraintViolation();
        }

        return matches;
    }
}
