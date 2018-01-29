
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
    "scheme",
    "id",
    "description",
    "uri"
})
public class Classification {
    @JsonProperty("id")
    @JsonPropertyDescription("The classification code drawn from the selected scheme.")
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A textual description or title for the code.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("scheme")
    @JsonPropertyDescription("An classification should be drawn from an existing scheme or list of codes. This field " +
        "is used to indicate the scheme/codelist from which the classification is drawn. For line item " +
        "classifications, this value should represent an known [Item Classification Scheme](http://standard" +
        ".open-contracting.org/latest/en/schema/codelists/#item-classification-scheme) wherever possible.")
    private final Scheme scheme;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI to identify the code. In the event individual URIs are not available for items in" +
        " the identifier scheme this value should be left blank.")
    private final URI uri;

    @JsonCreator
    public Classification(@JsonProperty("scheme") final Scheme scheme,
                          @JsonProperty("id") final String id,
                          @JsonProperty("description") final String description,
                          @JsonProperty("uri") final URI uri) {
        this.id = id;
        this.description = description;
        this.scheme = scheme;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scheme)
                                    .append(id)
                                    .append(description)
                                    .append(uri)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Classification)) {
            return false;
        }
        final Classification rhs = (Classification) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                                  .append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(uri, rhs.uri)
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
