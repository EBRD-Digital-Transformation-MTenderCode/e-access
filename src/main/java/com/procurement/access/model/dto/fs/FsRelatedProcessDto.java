package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.*;
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
        "title",
        "relationship",
        "scheme",
        "identifier",
        "uri"
})
public class FsRelatedProcessDto {
    @JsonProperty("id")
    @JsonPropertyDescription("A local identifier for this relationship, unique within this array.")
    @NotNull
    private final String id;

    @JsonProperty("relationship")
    @JsonPropertyDescription("Specify the type of relationship using the [related process codelist](http://standard" +
            ".open-contracting.org/latest/en/schema/codelists/#related-process).")
    private final List<RelatedProcessType> relationship;

    @JsonProperty("title")
    @JsonPropertyDescription("The title of the related process, where referencing an open contracting process, this " +
            "field should match the tender/title field in the related process.")
    private final String title;

    @JsonProperty("scheme")
    @JsonPropertyDescription("The identification scheme used by this cross-reference from the [related process scheme" +
            " codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#related-process-scheme) " +
            "codelist" +
            ". When cross-referencing information also published using OCDS, an Open Contracting ID (ocId) should be " +
            "used.")
    private final RelatedProcessScheme scheme;

    @JsonProperty("identifier")
    @JsonPropertyDescription("The identifier of the related process. When cross-referencing information also " +
            "published using OCDS, this should be the Open Contracting ID (ocId).")
    private final String identifier;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing to a machine-readable document, release or record package containing the" +
            " identified related process.")
    private final String uri;

    @JsonCreator
    public FsRelatedProcessDto(@JsonProperty("id") final String id,
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
        if (!(other instanceof FsRelatedProcessDto)) {
            return false;
        }
        final FsRelatedProcessDto rhs = (FsRelatedProcessDto) other;
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

        static final Map<String, RelatedProcessType> CONSTANTS = new HashMap<>();

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

    public enum RelatedProcessScheme {
        OCID("ocid");

        static final Map<String, RelatedProcessScheme> CONSTANTS = new HashMap<>();

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
