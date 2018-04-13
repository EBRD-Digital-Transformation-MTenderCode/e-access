package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum MainProcurementCategory {
    GOODS("goods"),
    WORKS("works"),
    SERVICES("services");

    private static final Map<String, MainProcurementCategory> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final MainProcurementCategory c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    MainProcurementCategory(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static MainProcurementCategory fromValue(final String value) {
        final MainProcurementCategory constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(MainProcurementCategory.class.getName(), value, Arrays.toString(values()));
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