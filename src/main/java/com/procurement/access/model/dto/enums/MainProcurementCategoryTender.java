package com.procurement.access.model.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;

public enum MainProcurementCategoryTender {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services");

    private static final Map<String, MainProcurementCategoryTender> CONSTANTS = new HashMap<>();

    private final String value;

    static {
        for (final MainProcurementCategoryTender c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    MainProcurementCategoryTender(final String value) {
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
    public static MainProcurementCategoryTender fromValue(final String value) {
        final MainProcurementCategoryTender constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        }
        return constant;
    }
}

