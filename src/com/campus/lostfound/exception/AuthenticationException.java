package com.campus.lostfound.exception;

/**
 * Thrown when user credentials fail or access is unauthorized.
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message);
    }
}
