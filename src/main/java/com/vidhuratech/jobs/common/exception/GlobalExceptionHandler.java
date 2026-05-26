package com.vidhuratech.jobs.common.exception;

import com.vidhuratech.jobs.common.api.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(
            ResourceNotFoundException ex
    ) {
        return build(
                HttpStatus.NOT_FOUND,
                safeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(
            DuplicateResourceException ex
    ) {
        return build(
                HttpStatus.CONFLICT,
                safeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(
            BusinessValidationException ex
    ) {
        return build(
                HttpStatus.BAD_REQUEST,
                safeMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(
            MethodArgumentNotValidException ex
    ) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return build(
                HttpStatus.BAD_REQUEST,
                safeMessage(errors)
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(
            RuntimeException ex
    ) {
        String message = safeMessage(ex.getMessage());

        switch (message) {
            case "INVALID_CREDENTIALS":
                return build(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                );

            case "USER_NOT_FOUND":
                return build(
                        HttpStatus.NOT_FOUND,
                        "Account not found"
                );

            case "ACCOUNT_INACTIVE":
                return build(
                        HttpStatus.FORBIDDEN,
                        "Account is inactive"
                );

            case "EMAIL_ALREADY_EXISTS":
                return build(
                        HttpStatus.CONFLICT,
                        "Email already exists"
                );

            case "CURRENT_PASSWORD_REQUIRED":
                return build(
                        HttpStatus.BAD_REQUEST,
                        "Current password is required"
                );

            case "NEW_PASSWORD_REQUIRED":
                return build(
                        HttpStatus.BAD_REQUEST,
                        "New password is required"
                );

            case "PASSWORD_TOO_SHORT":
                return build(
                        HttpStatus.BAD_REQUEST,
                        "New password must be at least 6 characters"
                );

            case "CURRENT_PASSWORD_INVALID":
                return build(
                        HttpStatus.UNAUTHORIZED,
                        "Current password is incorrect"
                );

            case "PASSWORD_SAME_AS_OLD":
                return build(
                        HttpStatus.BAD_REQUEST,
                        "New password must be different from current password"
                );

            default:
                return build(
                        HttpStatus.BAD_REQUEST,
                        message
                );
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(
            Exception ex
    ) {
        ex.printStackTrace();

        return build(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected server error"
        );
    }

    private String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Something went wrong";
        }

        return message;
    }

    private ResponseEntity<ApiResponse<Object>> build(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status)
                .body(
                        ApiResponse.builder()
                                .success(false)
                                .message(message)
                                .build()
                );
    }
}