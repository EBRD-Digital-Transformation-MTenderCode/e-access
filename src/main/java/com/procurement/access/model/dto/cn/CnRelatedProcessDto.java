package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "relationship",
        "title",
        "identifier",
        "scheme",
        "uri"
})
public class CnRelatedProcessDto {
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
            ". When cross-referencing information also published using OCDS, an Open Contracting ID (ocid) should be " +
            "used.")
    private final RelatedProcessScheme scheme;

    @JsonProperty("identifier")
    @JsonPropertyDescription("The identifier of the related process. When cross-referencing information also " +
            "published using OCDS, this should be the Open Contracting ID (ocid).")
    private final String identifier;

    @JsonProperty("uri")
    @JsonPropertyDescription("A URI pointing to a machine-readable document, release or record package containing the" +
            " identified related process.")
    private final String uri;

    @JsonCreator
    public CnRelatedProcessDto(@JsonProperty("id") final String id,
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

    public enum RelatedProcessType {
        FRAMEWORK("framework"),
        PLANNING("planning"),
        PARENT("parent"),
        PRIOR("prior"),
        UNSUCCESSFUL_PROCESS("unsuccessfulProcess"),
        SUB_CONTRACT("subContract"),
        REPLACEMENT_PROCESS("replacementProcess"),
        RENEWAL_PROCESS("renewalProcess"),
        X_EXPENDITURE_ITEM("x_expenditureItem"),
        X_BUDGET("x_budget"),
        X_FINANCE_SOURCE("x_financeSource"),
        X_PRESELECTION("x_preselection"),
        X_PREQUALIFICATION("x_prequalification"),
        X_PLANNED("x_planned"),
        X_CONTRACT("x_contract"),
        X_EXECUTION("x_execution");

        private static final Map<String, RelatedProcessType> CONSTANTS = new HashMap<>();

        static {
            for (final RelatedProcessType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private RelatedProcessType(final String value) {
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

        private static final Map<String, RelatedProcessScheme> CONSTANTS = new HashMap<>();

        static {
            for (final RelatedProcessScheme c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private RelatedProcessScheme(final String value) {
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
