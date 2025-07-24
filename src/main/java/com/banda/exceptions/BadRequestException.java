package com.banda.exceptions;


import com.banda.annotations.ResponseStatus;

/**
 * Thrown when the client has sent invalid data—map this to HTTP 400.
 */
@ResponseStatus(code = 400, reason = "Bad multipart payload")
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
