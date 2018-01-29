package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
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
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("scheme")
    private final Scheme scheme;

    @JsonProperty("value")
    private final Value value;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public Unit(@JsonProperty("name") final String name,
                @JsonProperty("value") final Value value,
                @JsonProperty("scheme") final Scheme scheme,
                @JsonProperty("id") final String id,
                @JsonProperty("uri") final String uri) {
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
