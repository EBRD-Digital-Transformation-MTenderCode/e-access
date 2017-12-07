package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum PnTenderStatusDto {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private static final Map<String, PnTenderStatusDto> CONSTANTS = new HashMap<>();

    static {
        for (final PnTenderStatusDto c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    PnTenderStatusDto(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static PnTenderStatusDto fromValue(final String value) {
        final PnTenderStatusDto constant = CONSTANTS.get(value);
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
