package com.antdevrealm.housechaosmain.review.exception;

public class ReviewServiceFeignCallException extends RuntimeException{
    public ReviewServiceFeignCallException(String message) {
        super(message);
    }

    public ReviewServiceFeignCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
