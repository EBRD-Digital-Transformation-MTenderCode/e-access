
package com.procurement.access.model.dto.cn;

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
    "uri",
    "period",
    "sourceParty"
})
public class CnBudgetBreakdownDto {
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
    private final CnValueDto amount;

    @JsonProperty("period")
    @Valid
    @NotNull
    private final CnBudgetBreakdownPeriodDto period;

    @JsonProperty("sourceParty")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
        "section")
    @Valid
    @NotNull
    private final CnOrganizationReferenceDto sourceParty;

    @JsonCreator
    public CnBudgetBreakdownDto(@JsonProperty("id") final String id,
                                @JsonProperty("description") final String description,
                                @JsonProperty("amount") final CnValueDto amount,
                                @JsonProperty("period") final CnBudgetBreakdownPeriodDto period,
                                @JsonProperty("sourceParty") final CnOrganizationReferenceDto sourceParty) {
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
        if (!(other instanceof CnBudgetBreakdownDto)) {
            return false;
        }
        final CnBudgetBreakdownDto rhs = (CnBudgetBreakdownDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(description, rhs.description)
                                  .append(amount, rhs.amount)
                                  .append(period, rhs.period)
                                  .append(sourceParty, rhs.sourceParty)
                                  .isEquals();
    }
}
