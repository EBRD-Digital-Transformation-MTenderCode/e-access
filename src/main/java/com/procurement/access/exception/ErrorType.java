package com.procurement.access.exception;

public enum ErrorType {

    DATA_NOT_FOUND("00.01", "Data not found."),
    INVALID_OWNER("00.02", "Invalid owner."),
    TENDER_ID_NOT_NULL("00.03", "Tender id must be empty ore null."),
    NOT_ACTIVE("10.01", "The tender procedure is not in active state."),
    NOT_INTERMEDIATE("10.02", "The tender procedure is not in any of the intermediate states."),
    NO_ACTIVE_LOTS("10.03", "There is no lot in the active state.");


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
