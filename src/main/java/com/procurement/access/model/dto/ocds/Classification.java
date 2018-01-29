package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
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
    @NotNull
    private final String id;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("scheme")
    private final Scheme scheme;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public Classification(@JsonProperty("scheme") final Scheme scheme,
                          @JsonProperty("id") final String id,
                          @JsonProperty("description") final String description,
                          @JsonProperty("uri") final String uri) {
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

        private final static Map<String, Scheme> CONSTANTS = new HashMap<>();

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
