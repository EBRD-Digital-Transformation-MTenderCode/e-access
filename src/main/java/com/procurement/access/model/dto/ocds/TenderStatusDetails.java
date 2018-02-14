package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum TenderStatusDetails {
    PRESELECTION("preselection"),
    PRESELECTED("preselected"),
    PREQUALIFICATION("prequalification"),
    PREQUALIFIED("prequalified"),
    EVALUATION("evaluation"),
    EVALUATED("evaluated"),
    EXECUTION("execution"),
    //**//
    PLANNING("planning"),
    PLANNED("planned"),
    ACTIVE("active"),
    BLOCKED("blocked"),
    CANCELLED("cancelled"),
    UNSUCCESSFUL("unsuccessful"),
    COMPLETE("complete"),
    WITHDRAWN("withdrawn"),
    SUSPENDED("suspended"),
    EMPTY("");

    private static final Map<String, TenderStatusDetails> CONSTANTS = new HashMap<>();

    static {
        for (final TenderStatusDetails c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private final String value;

    TenderStatusDetails(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static TenderStatusDetails fromValue(final String value) {
        final TenderStatusDetails constant = CONSTANTS.get(value);
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