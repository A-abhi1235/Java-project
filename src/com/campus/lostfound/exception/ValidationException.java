package com.campus.lostfound.exception;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when entity data fails business or format validation rules.
 */
public class ValidationException extends AppException {
    private final List<String> errorMessages;

    public ValidationException(String message) {
        super(message);
        this.errorMessages = Collections.singletonList(message);
    }

    public ValidationException(List<String> errors) {
        super(errors != null && !errors.isEmpty() ? String.join("\n", errors) : "Validation failed.");
        this.errorMessages = errors != null ? errors : Collections.emptyList();
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
}
