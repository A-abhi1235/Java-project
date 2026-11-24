package com.campus.lostfound.exception;

/**
 * Thrown when database connectivity, querying or persistence fails.
 */
public class DatabaseException extends AppException {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
