package com.procurement.access.exception;

import lombok.Getter;

@Getter
public class ErrorException extends RuntimeException {

    private String message;

    public ErrorException(final String message) {
        this.message = message;
    }
}
