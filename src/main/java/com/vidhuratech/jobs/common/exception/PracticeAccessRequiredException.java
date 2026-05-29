package com.vidhuratech.jobs.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PracticeAccessRequiredException extends RuntimeException {

    public PracticeAccessRequiredException(String message) {
        super(message);
    }
}