package com.example.videostreaming.exception;

import com.example.videostreaming.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling interceptor for REST controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles validation errors triggered by validation constraints (e.g. @NotBlank, @Size).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Validation failed for request: {}", ex.getBindingResult().getObjectName());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.warn("Field '{}' failed validation: {}", fieldName, errorMessage);
        });
        return ApiResponse.error("Request validation failed", errors);
    }

    /**
     * Handles ResourceNotFoundException, returning HTTP 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Requested resource was not found: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    /**
     * Handles StorageException, returning HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(StorageException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleStorageException(StorageException ex) {
        log.error("Storage system exception occurred: {}", ex.getMessage(), ex);
        return ApiResponse.error("An error occurred during file storage operation: " + ex.getMessage());
    }

    /**
     * Handles IllegalArgumentException/IllegalStateException, returning HTTP 400 Bad Request.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequestExceptions(RuntimeException ex) {
        log.warn("Bad request exception: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    /**
     * Catch-all exception handler to intercept unexpected server errors, returning HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGenericException(Exception ex) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return ApiResponse.error("An unexpected error occurred. Please contact system administrator.");
    }
}
