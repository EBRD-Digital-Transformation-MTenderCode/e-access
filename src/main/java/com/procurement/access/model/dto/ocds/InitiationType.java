package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum InitiationType {
    TENDER("tender");

    private static final Map<String, InitiationType> CONSTANTS = new HashMap<>();

    static {
        for (final InitiationType c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    InitiationType(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static InitiationType fromValue(final String value) {
        final InitiationType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(
                    "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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
