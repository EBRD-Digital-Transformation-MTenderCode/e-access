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
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "date",
        "status",
        "tenderers",
        "value",
        "documents",
        "relatedLots",
        "requirementResponses"
})
public class Bid {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime date;

    @JsonProperty("status")
    private final Status status;

    @JsonProperty("tenderers")
    private final List<OrganizationReference> tenderers;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("documents")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<Document> documents;

    @JsonProperty("relatedLots")
    private final List<String> relatedLots;

    @JsonProperty("requirementResponses")
    @Valid
    private final Set<RequirementResponse> requirementResponses;

    @JsonCreator
    public Bid(@JsonProperty("id") final String id,
               @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
               @JsonProperty("status") final Status status,
               @JsonProperty("tenderers") final List<OrganizationReference> tenderers,
               @JsonProperty("value") final Value value,
               @JsonProperty("documents") final LinkedHashSet<Document> documents,
               @JsonProperty("relatedLots") final List<String> relatedLots,
               @JsonProperty("requirementResponses") final LinkedHashSet<RequirementResponse> requirementResponses) {
        this.id = id;
        this.date = date;
        this.status = status;
        this.tenderers = tenderers;
        this.value = value;
        this.documents = documents;
        this.relatedLots = relatedLots;
        this.requirementResponses = requirementResponses;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(date)
                .append(status)
                .append(tenderers)
                .append(value)
                .append(documents)
                .append(relatedLots)
                .append(requirementResponses)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Bid)) {
            return false;
        }
        final Bid rhs = (Bid) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(date, rhs.date)
                .append(status, rhs.status)
                .append(tenderers, rhs.tenderers)
                .append(value, rhs.value)
                .append(documents, rhs.documents)
                .append(relatedLots, rhs.relatedLots)
                .append(requirementResponses, rhs.requirementResponses)
                .isEquals();
    }

    public enum Status {
        INVITED("invited"),
        PENDING("pending"),
        VALID("valid"),
        DISQUALIFIED("disqualified"),
        WITHDRAWN("withdrawn");

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
