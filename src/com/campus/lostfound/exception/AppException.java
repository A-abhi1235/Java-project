package com.campus.lostfound.exception;

/**
 * Root checked exception for Smart Campus application errors.
 * Demonstrates proper OOP custom exception hierarchies.
 */
public class AppException extends Exception {
    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
