package com.antdevrealm.housechaosmain.user.exception;

public class UserAlreadyHasRoleException extends RuntimeException{
    public UserAlreadyHasRoleException(String message) {
        super(message);
    }
}
