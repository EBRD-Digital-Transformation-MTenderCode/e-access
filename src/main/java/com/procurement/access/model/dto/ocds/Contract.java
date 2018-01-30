package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "awardID",
        "extendsContractID",
        "title",
        "description",
        "status",
        "period",
        "value",
        "items",
        "dateSigned",
        "documents",
        "implementation",
        "relatedProcesses",
        "milestones",
        "amendments",
        "amendment",
        "requirementResponses",
        "countryOfOrigin",
        "lotVariant",
        "valueBreakdown",
        "isFrameworkOrDynamic"
})
public class Contract {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("awardID")
    @NotNull
    private final String awardID;

    @JsonProperty("extendsContractID")
    private final String extendsContractID;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("status")
    private final Status status;

    @JsonProperty("period")
    @Valid
    private final Period period;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    private final Set<Item> items;

    @JsonProperty("dateSigned")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime dateSigned;

    @JsonProperty("documents")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Document> documents;

    @JsonProperty("implementation")
    @Valid
    private final Implementation implementation;

    @JsonProperty("relatedProcesses")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<RelatedProcess> relatedProcesses;

    @JsonProperty("milestones")
    @Valid
    private final List<Milestone> milestones;

    @JsonProperty("amendments")
    @Valid
    private final List<Amendment> amendments;

    @JsonProperty("amendment")
    @Valid
    private final Amendment amendment;

    @JsonProperty("requirementResponses")
    @Valid
    private final Set<RequirementResponse> requirementResponses;

    @JsonProperty("countryOfOrigin")
    private final String countryOfOrigin;

    @JsonProperty("lotVariant")
    private final Set<String> lotVariant;

    @JsonProperty("valueBreakdown")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<ValueBreakdown> valueBreakdown;

    @JsonProperty("isFrameworkOrDynamic")
    private final Boolean isFrameworkOrDynamic;

    @JsonCreator
    public Contract(@JsonProperty("id") final String id,
                    @JsonProperty("awardID") final String awardID,
                    @JsonProperty("extendsContractID") final String extendsContractID,
                    @JsonProperty("title") final String title,
                    @JsonProperty("description") final String description,
                    @JsonProperty("status") final Status status,
                    @JsonProperty("period") final Period period,
                    @JsonProperty("value") final Value value,
                    @JsonProperty("items") final LinkedHashSet<Item> items,
                    @JsonProperty("dateSigned") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime dateSigned,
                    @JsonProperty("documents") final LinkedHashSet<Document> documents,
                    @JsonProperty("implementation") final Implementation implementation,
                    @JsonProperty("relatedProcesses") final LinkedHashSet<RelatedProcess> relatedProcesses,
                    @JsonProperty("milestones") final List<Milestone> milestones,
                    @JsonProperty("amendments") final List<Amendment> amendments,
                    @JsonProperty("amendment") final Amendment amendment,
                    @JsonProperty("requirementResponses") final LinkedHashSet<RequirementResponse> requirementResponses,
                    @JsonProperty("countryOfOrigin") final String countryOfOrigin,
                    @JsonProperty("lotVariant") final LinkedHashSet<String> lotVariant,
                    @JsonProperty("valueBreakdown") final LinkedHashSet<ValueBreakdown> valueBreakdown,
                    @JsonProperty("isFrameworkOrDynamic") final Boolean isFrameworkOrDynamic) {
        this.id = id;
        this.awardID = awardID;
        this.extendsContractID = extendsContractID;
        this.title = title;
        this.description = description;
        this.status = status;
        this.period = period;
        this.value = value;
        this.items = items;
        this.dateSigned = dateSigned;
        this.documents = documents;
        this.implementation = implementation;
        this.relatedProcesses = relatedProcesses;
        this.milestones = milestones;
        this.amendments = amendments;
        this.amendment = amendment;
        this.requirementResponses = requirementResponses;
        this.countryOfOrigin = countryOfOrigin;
        this.lotVariant = lotVariant;
        this.valueBreakdown = valueBreakdown;
        this.isFrameworkOrDynamic = isFrameworkOrDynamic;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(awardID)
                .append(extendsContractID)
                .append(title)
                .append(description)
                .append(status)
                .append(period)
                .append(value)
                .append(items)
                .append(dateSigned)
                .append(documents)
                .append(implementation)
                .append(relatedProcesses)
                .append(milestones)
                .append(amendments)
                .append(amendment)
                .append(requirementResponses)
                .append(countryOfOrigin)
                .append(lotVariant)
                .append(valueBreakdown)
                .append(isFrameworkOrDynamic)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Contract)) {
            return false;
        }
        final Contract rhs = (Contract) other;

        return new EqualsBuilder().append(id, rhs.id)
                .append(awardID, rhs.awardID)
                .append(extendsContractID, rhs.extendsContractID)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .append(period, rhs.period)
                .append(value, rhs.value)
                .append(items, rhs.items)
                .append(dateSigned, rhs.dateSigned)
                .append(documents, rhs.documents)
                .append(implementation, rhs.implementation)
                .append(relatedProcesses, rhs.relatedProcesses)
                .append(milestones, rhs.milestones)
                .append(amendments, rhs.amendments)
                .append(amendment, rhs.amendment)
                .append(requirementResponses, rhs.requirementResponses)
                .append(countryOfOrigin, rhs.countryOfOrigin)
                .append(lotVariant, rhs.lotVariant)
                .append(valueBreakdown, rhs.valueBreakdown)
                .append(isFrameworkOrDynamic, rhs.isFrameworkOrDynamic)
                .isEquals();
    }

    public enum Status {
        PENDING("pending"),
        ACTIVE("active"),
        CANCELLED("cancelled"),
        TERMINATED("terminated");

        private final static Map<String, Status> CONSTANTS = new HashMap<>();

        static {
            for (final Status c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        Status(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static Status fromValue(final String value) {
            final Status constant = CONSTANTS.get(value);
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
}
