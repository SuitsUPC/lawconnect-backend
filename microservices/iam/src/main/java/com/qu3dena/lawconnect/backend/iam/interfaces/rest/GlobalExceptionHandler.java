package com.qu3dena.lawconnect.backend.iam.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for IAM service REST controllers.
 * <p>
 * Handles exceptions thrown by controllers and converts them to appropriate HTTP responses.
 * </p>
 *
 * @author LawConnect Team
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles RuntimeException exceptions.
     * <p>
     * Converts RuntimeException to appropriate HTTP status codes based on the error message.
     * </p>
     *
     * @param ex the RuntimeException to handle
     * @return ResponseEntity with appropriate HTTP status and error details
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", "/api/v1/authentication");

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        // Map specific error messages to appropriate HTTP status codes
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("Username already exists")) {
                status = HttpStatus.CONFLICT; // 409
                errorResponse.put("status", 409);
                errorResponse.put("error", "Conflict");
            } else if (message.contains("Role not found")) {
                status = HttpStatus.BAD_REQUEST; // 400
                errorResponse.put("status", 400);
                errorResponse.put("error", "Bad Request");
            } else if (message.contains("Invalid password")) {
                status = HttpStatus.BAD_REQUEST; // 400
                errorResponse.put("status", 400);
                errorResponse.put("error", "Bad Request");
            } else {
                errorResponse.put("status", 500);
                errorResponse.put("error", "Internal Server Error");
            }
        }

        return new ResponseEntity<>(errorResponse, status);
    }
}

