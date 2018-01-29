package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum ExtendedProcurementCategoryTender {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services"),
    CONSULTING_SERVICES("consultingServices");

    private static final Map<String, ExtendedProcurementCategoryTender> CONSTANTS = new HashMap<>();

    static {
        for (final ExtendedProcurementCategoryTender c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    ExtendedProcurementCategoryTender(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExtendedProcurementCategoryTender fromValue(final String value) {
        final ExtendedProcurementCategoryTender constant = CONSTANTS.get(value);
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
