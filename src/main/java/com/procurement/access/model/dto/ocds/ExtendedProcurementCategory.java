package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum ExtendedProcurementCategory {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    private static final Map<String, ExtendedProcurementCategory> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final ExtendedProcurementCategory c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    ExtendedProcurementCategory(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExtendedProcurementCategory fromValue(final String value) {
        final ExtendedProcurementCategory constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(ExtendedProcurementCategory.class.getName(), value, Arrays.toString(values()));
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