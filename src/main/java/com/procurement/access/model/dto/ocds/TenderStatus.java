package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TenderStatus {
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn");

    private static final Map<String, TenderStatus> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final TenderStatus c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    TenderStatus(final String value) {
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
    public static TenderStatus fromValue(final String value) {
        final TenderStatus constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(TenderStatus.class.getName(), value, Arrays.toString(values()));
        }
        return constant;
    }
}
