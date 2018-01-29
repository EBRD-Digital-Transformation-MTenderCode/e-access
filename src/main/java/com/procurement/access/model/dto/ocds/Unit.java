package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "value",
    "scheme",
    "id",
    "uri"
})
public class Unit {
    @JsonProperty("id")
    @JsonPropertyDescription("The identifier from the codelist referenced in the scheme property. Check the codelist " +
        "for details of how to find and use identifiers from the scheme in use.")
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("Name of the unit.")
    @Pattern(regexp = "^(name_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String name;

    @JsonProperty("scheme")
    @JsonPropertyDescription("The list from which units of measure identifiers are taken. This should be an entry " +
        "from the options available in the [unitClassificationScheme](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#unit-classification-scheme) codelist. Use of the scheme 'UNCEFACT' for the " +
        "UN/CEFACT Recommendation 20 list of 'Codes for Units of Measure Used in International Trade' is recommended," +
        " although other options are available.")
    private final Scheme scheme;

    @JsonProperty("value")
    private final Value value;

    @JsonProperty("uri")
    @JsonPropertyDescription("If the scheme used provide a machine-readable URI for this unit of measure, this can be" +
        " given.")
    private final URI uri;

    @JsonCreator
    public Unit(@JsonProperty("name") final String name,
                @JsonProperty("value") final Value value,
                @JsonProperty("scheme") final Scheme scheme,
                @JsonProperty("id") final String id,
                @JsonProperty("uri") final URI uri) {
        this.name = name;
        this.value = value;
        this.scheme = scheme;
        this.id = id;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                                    .append(value)
                                    .append(scheme)
                                    .append(id)
                                    .append(uri)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Unit)) {
            return false;
        }
        final Unit rhs = (Unit) other;
        return new EqualsBuilder().append(name, rhs.name)
                                  .append(value, rhs.value)
                                  .append(scheme, rhs.scheme)
                                  .append(id, rhs.id)
                                  .append(uri, rhs.uri)
                                  .isEquals();
    }

    public enum Scheme {
        UNCEFACT("UNCEFACT"),
        QUDT("QUDT");

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
