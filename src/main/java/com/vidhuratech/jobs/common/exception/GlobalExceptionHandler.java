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
                ex.getMessage()
        );
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicate(
            DuplicateResourceException ex
    ) {

        return build(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusiness(
            BusinessValidationException ex
    ) {

        return build(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
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
                errors
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntime(
            RuntimeException ex
    ) {

        switch (ex.getMessage()) {

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

            default:
                return build(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
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