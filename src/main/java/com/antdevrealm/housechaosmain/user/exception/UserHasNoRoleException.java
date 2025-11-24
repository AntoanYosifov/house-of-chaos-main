package com.antdevrealm.housechaosmain.user.exception;

public class UserHasNoRoleException extends RuntimeException{
    public UserHasNoRoleException(String message) {
        super(message);
    }
}
