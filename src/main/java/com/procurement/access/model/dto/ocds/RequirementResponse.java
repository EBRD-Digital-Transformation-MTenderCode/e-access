package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "value",
        "period",
        "requirement",
        "relatedTenderer"
})
public class RequirementResponse {
    @JsonProperty("id")
    @NotNull
    private final String id;

    @JsonProperty("title")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("value")
    private final String value;

    @JsonProperty("period")
    @Valid
    private final Period period;

    @JsonProperty("requirement")
    @Valid
    @NotNull
    private final RequirementReference requirement;

    @JsonProperty("relatedTenderer")
    @Valid
    private final OrganizationReference relatedTenderer;

    @JsonCreator
    public RequirementResponse(@JsonProperty("id") final String id,
                               @JsonProperty("title") final String title,
                               @JsonProperty("description") final String description,
                               @JsonProperty("value") final String value,
                               @JsonProperty("period") final Period period,
                               @JsonProperty("requirement") final RequirementReference requirement,
                               @JsonProperty("relatedTenderer") final OrganizationReference relatedTenderer) {
        super();
        this.id = id;
        this.title = title;
        this.description = description;
        this.value = value;
        this.period = period;
        this.requirement = requirement;
        this.relatedTenderer = relatedTenderer;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(value)
                .append(period)
                .append(requirement)
                .append(relatedTenderer)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RequirementResponse)) {
            return false;
        }
        final RequirementResponse rhs = (RequirementResponse) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(value, rhs.value)
                .append(period, rhs.period)
                .append(requirement, rhs.requirement)
                .append(relatedTenderer, rhs.relatedTenderer)
                .isEquals();
    }
}
