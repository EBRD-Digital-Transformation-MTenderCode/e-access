package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum SubmissionMethod {
    ELECTRONIC_SUBMISSION("electronicSubmission"),
    ELECTRONIC_AUCTION("electronicAuction"),
    WRITTEN("written"),
    IN_PERSON("inPerson");

    private static final Map<String, SubmissionMethod> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final SubmissionMethod c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    SubmissionMethod(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static SubmissionMethod fromValue(final String value) {
        final SubmissionMethod constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(SubmissionMethod.class.getName(), value, Arrays.toString(values()));
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
