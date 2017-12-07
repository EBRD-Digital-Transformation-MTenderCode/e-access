package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum CnTenderStatusDto {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private static final Map<String, CnTenderStatusDto> CONSTANTS = new HashMap<>();

    static {
        for (final CnTenderStatusDto c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    CnTenderStatusDto(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static CnTenderStatusDto fromValue(final String value) {
        final CnTenderStatusDto constant = CONSTANTS.get(value);
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
