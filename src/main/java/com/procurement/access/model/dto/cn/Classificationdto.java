
package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "scheme",
    "id"
})
public class Classificationdto {
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



    @JsonCreator
    public Classificationdto(@JsonProperty("scheme") final Scheme scheme,
                             @JsonProperty("id") final String id) {
        this.id = id;
        this.scheme = scheme;

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scheme)
                                    .append(id)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Classificationdto)) {
            return false;
        }
        final Classificationdto rhs = (Classificationdto) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                                  .append(id, rhs.id)
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

        private final String value;
        private final static Map<String, Scheme> CONSTANTS = new HashMap<>();

        static {
            for (final Scheme c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private Scheme(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static Scheme fromValue(final String value) {
            final Scheme constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}
