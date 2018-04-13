package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum SubmissionMethodRationale {
    TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
    IPR_ISSUES("IPR_ISSUES"),
    REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
    PHYSICAL_MODEL("PHYSICAL_MODEL"),
    SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

    private static final Map<String, SubmissionMethodRationale> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final SubmissionMethodRationale c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    SubmissionMethodRationale(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static SubmissionMethodRationale fromValue(final String value) {
        final SubmissionMethodRationale constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(SubmissionMethodRationale.class.getName(), value, Arrays.toString(values()));
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
