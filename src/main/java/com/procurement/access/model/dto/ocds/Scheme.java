package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum Scheme {
    CPV("CPV"),
    CPVS("CPVS"),
    GSIN("GSIN"),
    UNSPSC("UNSPSC"),
    CPC("CPC"),
    OKDP("OKDP"),
    OKPD("OKPD");

    private static final Map<String, Scheme> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final Scheme c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    Scheme(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static Scheme fromValue(final String value) {
        final Scheme constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(Scheme.class.getName(), value, Arrays.toString(values()));
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