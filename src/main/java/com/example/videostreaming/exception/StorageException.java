package com.example.videostreaming.exception;

/**
 * Exception thrown when an error occurs during an operation on the storage service.
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
