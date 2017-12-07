
package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "description",
    "amount",
    "period",
    "sourceParty"
})
public class PinBudgetBreakdownDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this particular budget entry.")
    @NotNull
    private final String id;

    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of this budget entry.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String description;

    @JsonProperty("amount")
    @Valid
    @NotNull
    private final PinValueDto amount;

    @JsonProperty("period")
    @Valid
    @NotNull
    private final PinPeriodDto period;

    @JsonProperty("sourceParty")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
        "section")
    @Valid
    @NotNull
    private final PinOrganizationReferenceDto sourceParty;

    @JsonCreator
    public PinBudgetBreakdownDto(@JsonProperty("id") final String id,
                                 @JsonProperty("description") final String description,
                                 @JsonProperty("amount") final PinValueDto amount,
                                 @JsonProperty("period") final PinPeriodDto period,
                                 @JsonProperty("sourceParty") final PinOrganizationReferenceDto sourceParty) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.period = period;
        this.sourceParty = sourceParty;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
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
        if (!(other instanceof PinBudgetBreakdownDto)) {
            return false;
        }
        final PinBudgetBreakdownDto rhs = (PinBudgetBreakdownDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(amount, rhs.amount)
                                  .append(period, rhs.period)
                                  .append(sourceParty, rhs.sourceParty)
                                  .isEquals();
    }
}
