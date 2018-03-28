package com.procurement.access.exception;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {

    private String code;
    private String message;

    public ErrorException(ErrorType error) {
        this.code = error.getCode();
        this.message = error.getMessage();
    }
}
