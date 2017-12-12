package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ocid",
    "title",
    "relationship",
    "scheme",
    "uri"
})
public class FsRelatedProcessDto {
    @JsonProperty("ocid")
    @JsonPropertyDescription("A local identifier for this relationship, unique within this array.")
    @NotNull
    private final String ocid;

    @JsonProperty("relationship")
    @JsonPropertyDescription("Specify the type of relationship using the [related process codelist](http://standard" +
        ".open-contracting.org/latest/en/schema/codelists/#related-process).")
    private final RelatedProcessType relationship;

    @JsonProperty("title")
    @JsonPropertyDescription("The title of the related process, where referencing an open contracting process, this " +
        "field should match the tender/title field in the related process.")
    private final String title;

    @JsonProperty("scheme")
    @JsonPropertyDescription("The identification scheme used by this cross-reference from the [related process scheme" +
        " codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#related-process-scheme) codelist" +
        ". When cross-referencing information also published using OCDS, an Open Contracting ID (ocid) should be used.")
    private final RelatedProcessScheme scheme;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing to a machine-readable document, release or record package containing the" +
        " identified related process.")
    private final String uri;

    @JsonCreator
    public FsRelatedProcessDto(@JsonProperty("ocid") final String ocid,
                               @JsonProperty("relationship") final RelatedProcessType relationship,
                               @JsonProperty("title") final String title,
                               @JsonProperty("scheme") final RelatedProcessScheme scheme,
                               @JsonProperty("identifier") final String identifier,
                               @JsonProperty("uri") final String uri) {
        this.ocid = ocid;
        this.relationship = relationship;
        this.title = title;
        this.scheme = scheme;
        this.uri = uri;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ocid)
                                    .append(relationship)
                                    .append(title)
                                    .append(scheme)
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
        return new EqualsBuilder().append(ocid, rhs.ocid)
                                  .append(relationship, rhs.relationship)
                                  .append(title, rhs.title)
                                  .append(scheme, rhs.scheme)
                                  .append(uri, rhs.uri)
                                  .isEquals();
    }

    public enum RelatedProcessType {
        FRAMEWORK("framework"),
        PLANNING("planning"),
        PARENT("parent"),
        PRIOR("prior"),
        UNSUCCESSFUL_PROCESS("unsuccessfulProcess"),
        SUB_CONTRACT("subContract"),
        REPLACEMENT_PROCESS("replacementProcess"),
        RENEWAL_PROCESS("renewalProcess");

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
