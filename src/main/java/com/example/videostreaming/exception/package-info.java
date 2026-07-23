/**
 * <h2>Exception Package</h2>
 * <p><strong>Why it exists:</strong> To manage custom runtime exceptions, error messages, and global controller advisors to ensure robust, centralized error handling.</p>
 * <p><strong>What belongs here:</strong> Custom exceptions (e.g., ResourceNotFoundException, InvalidPayloadException), error details classes, and {@code @RestControllerAdvice} handlers.</p>
 * <p><strong>What should never be placed here:</strong> Data mapper interfaces, service layers, controllers (except the exception handler advisor), or JPA entities.</p>
 */
package com.example.videostreaming.exception;
