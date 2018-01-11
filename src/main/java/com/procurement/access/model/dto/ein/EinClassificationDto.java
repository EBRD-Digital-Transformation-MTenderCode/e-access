package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
        "id",
        "scheme",
        "description"
})
public class EinClassificationDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The classification code drawn from the selected scheme.")
    @NotNull
    private final String id;

    @JsonProperty("scheme")
    @JsonPropertyDescription("An classification should be drawn from an existing scheme or list of codes. This field " +
            "is used to indicate the scheme/codelist from which the classification is drawn. For line item " +
            "classifications, this value should represent an known [ItemDto Classificationdto Scheme](http://standard" +
            ".open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) wherever possible.")
    @NotNull
    private final Scheme scheme;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the classification.")
    @NotNull
    private final String description;

    @JsonCreator
    public EinClassificationDto(@JsonProperty("id") final String id,
                                @JsonProperty("scheme") final Scheme scheme,
                                @JsonProperty("description") final String description) {
        this.id = id;
        this.scheme = scheme;
        this.description = description;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(scheme)
                .append(description)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EinClassificationDto)) {
            return false;
        }
        final EinClassificationDto rhs = (EinClassificationDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(scheme, rhs.scheme)
                .append(description, rhs.description)
                .isEquals();
    }

    public enum Scheme {
        CPV("CPV"),
        CPVS("CPVS"),
        GSIN("GSIN"),
        UNSPSC("UNSPSC"),
        CPC("CPC"),
        OKDP("OKDP"),
        OKPD("OKPD");

        static final Map<String, Scheme> CONSTANTS = new HashMap<>();

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
