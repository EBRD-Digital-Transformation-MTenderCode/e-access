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
        "description",
        "amount",
        "period",
        "sourceParty"
})
public class BudgetBreakdown {

    @NotNull
    @JsonProperty("id")
    private final String id;

    @JsonProperty("description")
    private final String description;

    @Valid
    @NotNull
    @JsonProperty("amount")
    private final Value amount;

    @Valid
    @NotNull
    @JsonProperty("period")
    private final Period period;

    @Valid
    @NotNull
    @JsonProperty("sourceParty")
    private final OrganizationReference sourceParty;

    @JsonCreator
    public BudgetBreakdown(@JsonProperty("id") final String id,
                           @JsonProperty("description") final String description,
                           @JsonProperty("amount") final Value amount,
                           @JsonProperty("period") final Period period,
                           @JsonProperty("sourceParty") final OrganizationReference sourceParty) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.period = period;
        this.sourceParty = sourceParty;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(description)
                .append(amount)
                .append(period)
                .append(sourceParty)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BudgetBreakdown)) {
            return false;
        }
        final BudgetBreakdown rhs = (BudgetBreakdown) other;
        return new EqualsBuilder()
                .append(id, rhs.id)
                .append(description, rhs.description)
                .append(amount, rhs.amount)
                .append(period, rhs.period)
                .append(sourceParty, rhs.sourceParty)
                .isEquals();
    }

}
