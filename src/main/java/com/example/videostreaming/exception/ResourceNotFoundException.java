package com.example.videostreaming.exception;

/**
 * Exception thrown when a requested resource (e.g. video) is not found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
