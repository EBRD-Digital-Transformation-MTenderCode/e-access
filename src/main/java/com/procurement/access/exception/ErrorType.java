package com.procurement.access.exception;

public enum ErrorType {

    DATA_NOT_FOUND("00.01", "Data not found."),
    INVALID_OWNER("00.02", "Invalid owner."),
    OCID_NOT_NULL("00.03", "Ocid must be empty."),
    TOKEN_NOT_NULL("00.04", "Token id must be empty."),
    TENDER_ID_NOT_NULL("00.05", "Tender id must be empty."),
    TENDER_STATUS_NOT_NULL("00.06", "Tender status must be empty."),
    TENDER_STATUS_DETAILS_NOT_NULL("00.07", "Tender status details must be empty."),
    LOT_STATUS_NOT_NULL("00.08", "Lot status must be empty."),
    LOT_STATUS_DETAILS_NOT_NULL("00.09", "Lot status details must be empty."),
    PERIOD_NOT_NULL("00.10", "Tender period must be empty."),
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
