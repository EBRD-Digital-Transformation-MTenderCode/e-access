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
        "title",
        "description",
        "status",
        "date",
        "value",
        "suppliers",
        "items",
        "contractPeriod",
        "documents",
        "amendments",
        "amendment",
        "relatedLots",
        "requirementResponses",
        "reviewProceedings",
        "statusDetails"
})
public class Award {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("status")
    private final Status status;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime date;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("suppliers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<OrganizationReference> suppliers;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Size(min = 1)
    @Valid
    private final Set<Item> items;

    @JsonProperty("contractPeriod")
    @Valid
    private final Period contractPeriod;

    @JsonProperty("documents")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Document> documents;

    @JsonProperty("amendments")
    @Valid
    private final List<Amendment> amendments;

    @JsonProperty("amendment")
    @Valid
    private final Amendment amendment;

    @JsonProperty("relatedLots")
    private final List<String> relatedLots;

    @JsonProperty("requirementResponses")
    @Valid
    private final Set<RequirementResponse> requirementResponses;

    @JsonProperty("reviewProceedings")
    @Valid
    private final ReviewProceedings reviewProceedings;

    @JsonProperty("statusDetails")
    private final String statusDetails;

    @JsonProperty("relatedBid")
    private final String relatedBid;

    @JsonCreator
    public Award(@JsonProperty("id") final String id,
                 @JsonProperty("title") final String title,
                 @JsonProperty("description") final String description,
                 @JsonProperty("status") final Status status,
                 @JsonProperty("date") final LocalDateTime date,
                 @JsonProperty("value") final Value value,
                 @JsonProperty("suppliers") final LinkedHashSet<OrganizationReference> suppliers,
                 @JsonProperty("items") final LinkedHashSet<Item> items,
                 @JsonProperty("contractPeriod") final Period contractPeriod,
                 @JsonProperty("documents") final LinkedHashSet<Document> documents,
                 @JsonProperty("amendments") final List<Amendment> amendments,
                 @JsonProperty("amendment") final Amendment amendment,
                 @JsonProperty("relatedLots") final List<String> relatedLots,
                 @JsonProperty("requirementResponses") final LinkedHashSet<RequirementResponse> requirementResponses,
                 @JsonProperty("reviewProceedings") final ReviewProceedings reviewProceedings,
                 @JsonProperty("statusDetails") final String statusDetails,
                 @JsonProperty("relatedBid") final String relatedBid) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.date = date;
        this.value = value;
        this.suppliers = suppliers;
        this.items = items;
        this.contractPeriod = contractPeriod;
        this.documents = documents;
        this.amendments = amendments;
        this.amendment = amendment;
        this.relatedLots = relatedLots;
        this.requirementResponses = requirementResponses;
        this.reviewProceedings = reviewProceedings;
        this.statusDetails = statusDetails;
        this.relatedBid = relatedBid;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(status)
                .append(date)
                .append(value)
                .append(suppliers)
                .append(items)
                .append(contractPeriod)
                .append(documents)
                .append(amendments)
                .append(amendment)
                .append(relatedLots)
                .append(requirementResponses)
                .append(reviewProceedings)
                .append(statusDetails)
                .append(relatedBid)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Award)) {
            return false;
        }
        final Award rhs = (Award) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .append(date, rhs.date)
                .append(value, rhs.value)
                .append(suppliers, rhs.suppliers)
                .append(items, rhs.items)
                .append(contractPeriod, rhs.contractPeriod)
                .append(documents, rhs.documents)
                .append(amendments, rhs.amendments)
                .append(amendment, rhs.amendment)
                .append(relatedLots, rhs.relatedLots)
                .append(requirementResponses, rhs.requirementResponses)
                .append(reviewProceedings, rhs.reviewProceedings)
                .append(statusDetails, rhs.statusDetails)
                .append(relatedBid, rhs.relatedBid)
                .isEquals();
    }

    public enum Status {
        PENDING("pending"),
        ACTIVE("active"),
        CANCELLED("cancelled"),
        UNSUCCESSFUL("unsuccessful");

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
