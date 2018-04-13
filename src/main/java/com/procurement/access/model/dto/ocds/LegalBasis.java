package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum LegalBasis {
    DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
    DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
    DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
    DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
    REGULATION_966_2012("REGULATION_966_2012"),
    NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
    NULL("NULL");

    private static final Map<String, LegalBasis> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final LegalBasis c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    LegalBasis(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static LegalBasis fromValue(final String value) {
        final LegalBasis constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(LegalBasis.class.getName(), value, Arrays.toString(values()));
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