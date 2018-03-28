package com.procurement.access.exception;

import java.util.HashMap;
import java.util.Map;

public enum ErrorType {

    NOT_ACTIVE("3.10.01", "The tender procedure is not in active state."),
    NOT_INTERMEDIATE("3.10.02", "The tender procedure is not in any of the intermediate states."),
    NO_ACTIVE_LOTS("3.10.03", "There is no lot in the active state."),
    DATA_NOT_FOUND("3.10.04", "Data not found."),
    INVALID_OWNER("3.10.05", "Invalid owner.");

    private final static Map<String, ErrorType> CONSTANTS = new HashMap<>();

    private final String code;
    private final String message;

    ErrorType(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
