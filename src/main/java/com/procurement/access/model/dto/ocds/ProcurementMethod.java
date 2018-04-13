package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ProcurementMethod {
    OPEN("open"),
    SELECTIVE("selective"),
    LIMITED("limited"),
    DIRECT("direct");

    private static final Map<String, ProcurementMethod> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final ProcurementMethod c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    ProcurementMethod(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static ProcurementMethod fromValue(final String value) {
        final ProcurementMethod constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(ProcurementMethod.class.getName(), value, Arrays.toString(values()));
        }
        return constant;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}