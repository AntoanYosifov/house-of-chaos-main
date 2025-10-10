package com.antdevrealm.housechaosmain.features.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//TODO: Handle it via a global @ControllerAdvice
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class RefreshTokenInvalidException extends RuntimeException{
}
