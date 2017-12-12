package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum EinTenderStatusDetailsDto {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private static final Map<String, EinTenderStatusDetailsDto> CONSTANTS = new HashMap<>();

    private final String value;

    static {
        for (final EinTenderStatusDetailsDto c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    EinTenderStatusDetailsDto(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static EinTenderStatusDetailsDto fromValue(final String value) {
        final EinTenderStatusDetailsDto constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }
}
