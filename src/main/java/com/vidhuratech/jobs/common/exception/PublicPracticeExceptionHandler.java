package com.vidhuratech.jobs.common.exception;

import com.vidhuratech.jobs.common.api.ApiResponse;
import com.vidhuratech.jobs.common.exception.AlreadyRegisteredException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "com.vidhuratech.jobs.publicpractice")
public class PublicPracticeExceptionHandler {

    @ExceptionHandler(AlreadyRegisteredException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<?> handleAlreadyRegistered(AlreadyRegisteredException ex) {
        return ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> handleRuntime(RuntimeException ex) {
        return ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
    }
}