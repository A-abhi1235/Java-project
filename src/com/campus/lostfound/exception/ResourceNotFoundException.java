package com.campus.lostfound.exception;

/**
 * Thrown when a requested item, user, or claim record cannot be found.
 */
public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
