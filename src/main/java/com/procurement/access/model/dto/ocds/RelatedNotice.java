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
        "scheme",
        "id",
        "relationship",
        "objectOfProcurementInPIN",
        "uri"
})
public class RelatedNotice {
    @JsonProperty("scheme")
    private final RelatedNotice.Scheme scheme;

    @JsonProperty("id")
    private final String id;

    @JsonProperty("relationship")
    private final RelatedNotice.Relationship relationship;

    @JsonProperty("objectOfProcurementInPIN")
    private final String objectOfProcurementInPIN;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public RelatedNotice(@JsonProperty("scheme") final Scheme scheme,
                         @JsonProperty("id") final String id,
                         @JsonProperty("relationship") final Relationship relationship,
                         @JsonProperty("objectOfProcurementInPIN") final String objectOfProcurementInPIN,
                         @JsonProperty("uri") final String uri) {
        this.scheme = scheme;
        this.id = id;
        this.relationship = relationship;
        this.objectOfProcurementInPIN = objectOfProcurementInPIN;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(scheme)
                .append(id)
                .append(relationship)
                .append(objectOfProcurementInPIN)
                .append(uri)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RelatedNotice)) {
            return false;
        }
        final RelatedNotice rhs = (RelatedNotice) other;
        return new EqualsBuilder().append(scheme, rhs.scheme)
                .append(id, rhs.id)
                .append(relationship, rhs.relationship)
                .append(objectOfProcurementInPIN, rhs.objectOfProcurementInPIN)
                .append(uri, rhs.uri)
                .isEquals();
    }

    public enum Relationship {
        PREVIOUS_NOTICE("previousNotice");
        private final static Map<String, Relationship> CONSTANTS = new HashMap<>();

        static {
            for (final Relationship c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        Relationship(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Relationship fromValue(final String value) {
            final Relationship constant = CONSTANTS.get(value);
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

    public enum Scheme {
        TED("TED"),
        NATIONAL("National");
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
            final RelatedNotice.Scheme constant = CONSTANTS.get(value);
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
