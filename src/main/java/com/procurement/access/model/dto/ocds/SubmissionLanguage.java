package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum SubmissionLanguage {
    BG("bg"),
    ES("es"),
    CS("cs"),
    DA("da"),
    DE("de"),
    ET("et"),
    EL("el"),
    EN("en"),
    FR("fr"),
    GA("ga"),
    HR("hr"),
    IT("it"),
    LV("lv"),
    LT("lt"),
    HU("hu"),
    MT("mt"),
    NL("nl"),
    PL("pl"),
    PT("pt"),
    RO("ro"),
    SK("sk"),
    SL("sl"),
    FI("fi"),
    SV("sv");

    private static final Map<String, SubmissionLanguage> CONSTANTS = new HashMap<>();
    private final String value;

    static {
        for (final SubmissionLanguage c : values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    SubmissionLanguage(final String value) {
        this.value = value;
    }

    @JsonCreator
    public static SubmissionLanguage fromValue(final String value) {
        final SubmissionLanguage constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new EnumException(SubmissionLanguage.class.getName(), value, Arrays.toString(values()));
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
