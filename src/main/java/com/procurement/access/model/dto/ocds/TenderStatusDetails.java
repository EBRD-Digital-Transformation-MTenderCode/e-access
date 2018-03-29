package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
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
    EMPTY("empty");

    private static final Map<String, TenderStatusDetails> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final TenderStatusDetails c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    TenderStatusDetails(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static TenderStatusDetails fromValue(final String value) {
        final TenderStatusDetails constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(TenderStatusDetails.class.getName(), value, Arrays.toString(values()));
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
