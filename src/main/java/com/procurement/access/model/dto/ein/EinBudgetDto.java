package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
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
    "period",
    "amount"
})
public class EinBudgetDto {
    @NotNull
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for the budget line item which provides funds for this contracting " +
        "process. This identifier should be possible to cross-reference against the provided data source.")
    private final String id;

    @JsonProperty("period")
    @JsonPropertyDescription("The period covered by this budget entry.")
    @Valid
    @NotNull
    private final EinPeriodDto period;

    @JsonProperty("amount")
    @Valid
    @NotNull
    private final EinValueDto amount;

    @JsonCreator
    public EinBudgetDto(@JsonProperty("id") final String id,
                        @JsonProperty("period") final EinPeriodDto period,
                        @JsonProperty("amount") final EinValueDto amount
    ) {
        this.id = id;
        this.amount = amount;
        this.period = period;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(amount)
                                    .append(period)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof EinBudgetDto)) {
            return false;
        }
        final EinBudgetDto rhs = (EinBudgetDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(amount, rhs.amount)
                                  .append(period, rhs.period)
                                  .isEquals();
    }
}
