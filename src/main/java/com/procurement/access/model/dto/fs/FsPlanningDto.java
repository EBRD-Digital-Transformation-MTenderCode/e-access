package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
        "budget",
        "rationale"
})
public class FsPlanningDto {

    @JsonProperty("budget")
    @JsonPropertyDescription("This section contain information about the budget line, and associated projects, " +
            "through which this contracting process is funded. It draws upon data model of the [Fiscal Data Package]" +
            "(http://fiscal.dataprotocols.org/), and should be used to cross-reference to more detailed information " +
            "held " +
            "using a BudgetDto Data Package, or, where no linked BudgetDto Data Package is available, to provide " +
            "enough " +
            "information to allow a user to manually or automatically cross-reference with another published source " +
            "of " +
            "budget and project information.")
    @Valid
    @NotNull
    private final FsBudgetDto budget;

    @JsonProperty("rationale")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String rationale;

    @JsonCreator
    public FsPlanningDto(@JsonProperty("budget") final FsBudgetDto budget,
                         @JsonProperty("rationale") final String rationale) {
        this.budget = budget;
        this.rationale = rationale;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(budget)
                .append(rationale)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FsPlanningDto)) {
            return false;
        }
        final FsPlanningDto rhs = (FsPlanningDto) other;
        return new EqualsBuilder().append(budget, rhs.budget)
                .append(rationale, rhs.rationale)
                .isEquals();
    }
}
