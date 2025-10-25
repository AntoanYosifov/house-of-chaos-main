package com.antdevrealm.housechaosmain.features.auth.exception;

public class RefreshTokenInvalidException extends RuntimeException{
    public RefreshTokenInvalidException(String message) {
        super(message);
    }
}
