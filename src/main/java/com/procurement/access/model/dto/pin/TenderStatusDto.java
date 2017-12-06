package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum TenderStatusDto {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private final String value;
    private final static Map<String, TenderStatusDto> CONSTANTS = new HashMap<>();

    static {
        for (final TenderStatusDto c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private TenderStatusDto(final String value) {
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
    public static TenderStatusDto fromValue(final String value) {
        final TenderStatusDto constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }
}
