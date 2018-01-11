package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "scheme",
        "description",
        "id"
})
public class CnClassificationDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The classification code drawn from the selected scheme.")
    @NotNull
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A textual description or title for the code.")
    private final String description;

    @JsonProperty("scheme")
    @JsonPropertyDescription("An classification should be drawn from an existing scheme or list of codes. This field " +
            "is used to indicate the scheme/codelist from which the classification is drawn. For line item " +
            "classifications, this value should represent an known [CnItemDto CnClassificationDto Scheme]" +
            "(http://standard" +
            ".open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) wherever possible.")
    @NotNull
    private final Scheme scheme;

    @JsonCreator
    public CnClassificationDto(@JsonProperty("scheme") final Scheme scheme,
                               @JsonProperty("description") final String description,
                               @JsonProperty("id") final String id) {
        this.id = id;
        this.description = description;
        this.scheme = scheme;
    }

    public enum Scheme {
        CPV("CPV"),
        CPVS("CPVS"),
        GSIN("GSIN"),
        UNSPSC("UNSPSC"),
        CPC("CPC"),
        OKDP("OKDP"),
        OKPD("OKPD");

        private static final Map<String, Scheme> CONSTANTS = new HashMap<>();

        static {
            for (final Scheme c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        Scheme(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Scheme fromValue(final String value) {
            final Scheme constant = CONSTANTS.get(value);
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
}
