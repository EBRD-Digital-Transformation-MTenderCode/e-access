package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "relationship",
        "title",
        "scheme",
        "identifier",
        "uri"
})
public class RelatedProcess {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("relationship")
    private final List<RelatedProcessType> relationship;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("scheme")
    private final RelatedProcessScheme scheme;

    @JsonProperty("identifier")
    private final String identifier;

    @JsonProperty("uri")
    private final String uri;

    @JsonCreator
    public RelatedProcess(@JsonProperty("id") final String id,
                          @JsonProperty("relationship") final List<RelatedProcessType> relationship,
                          @JsonProperty("title") final String title,
                          @JsonProperty("scheme") final RelatedProcessScheme scheme,
                          @JsonProperty("identifier") final String identifier,
                          @JsonProperty("uri") final String uri) {
        this.id = id;
        this.relationship = relationship;
        this.title = title;
        this.scheme = scheme;
        this.identifier = identifier;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(relationship)
                .append(title)
                .append(scheme)
                .append(identifier)
                .append(uri)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RelatedProcess)) {
            return false;
        }
        final RelatedProcess rhs = (RelatedProcess) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(relationship, rhs.relationship)
                .append(title, rhs.title)
                .append(scheme, rhs.scheme)
                .append(identifier, rhs.identifier)
                .append(uri, rhs.uri)
                .isEquals();
    }

    public enum RelatedProcessType {
        FRAMEWORK("framework"),
        PLANNING("planning"),
        PARENT("parent"),
        PRIOR("prior"),
        UNSUCCESSFUL_PROCESS("unsuccessfulProcess"),
        REPLACEMENT_PROCESS("replacementProcess"),
        RENEWAL_PROCESS("renewalProcess"),
        SUB_CONTRACT("subContract"),
        X_EXPENDITURE_ITEM("x_expenditureItem"),
        X_FINANCE_SOURCE("x_financeSource"),
        X_PRESELECTION("x_preselection"),
        X_EXECUTION("x_execution"),
        X_PLANNED("x_planned"),
        X_BUDGET("x_budget");

        private final static Map<String, RelatedProcessType> CONSTANTS = new HashMap<>();

        static {
            for (final RelatedProcessType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        RelatedProcessType(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static RelatedProcessType fromValue(final String value) {
            final RelatedProcessType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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

    public enum RelatedProcessScheme {
        OCID("ocid");

        private final static Map<String, RelatedProcessScheme> CONSTANTS = new HashMap<>();

        static {
            for (final RelatedProcessScheme c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        RelatedProcessScheme(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static RelatedProcessScheme fromValue(final String value) {
            final RelatedProcessScheme constant = CONSTANTS.get(value);
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
