package com.procurement.access.exception;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {

    private final String code;
    private final String message;

    public ErrorException(final ErrorType error) {
        this.code = error.getCode();
        this.message = error.getMessage();
    }
}
