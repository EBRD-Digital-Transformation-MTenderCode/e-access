package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum PinTenderStatusDto {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private static final Map<String, PinTenderStatusDto> CONSTANTS = new HashMap<>();

    static {
        for (final PinTenderStatusDto c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    PinTenderStatusDto(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static PinTenderStatusDto fromValue(final String value) {
        final PinTenderStatusDto constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }
}
