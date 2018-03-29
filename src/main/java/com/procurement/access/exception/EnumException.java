package com.procurement.access.exception;

import lombok.Getter;

@Getter
public class EnumException extends RuntimeException {

    private final String code;
    private final String message;

    public EnumException(final String enumType, final String value, final String values) {
        this.code = "00.00";
        this.message = ("Unknown value for enumType " + enumType + ": " + value + ", Allowed values are " + values);
    }
}

