
package com.ocds.access.model.dto.ein;

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
    "budget",
    "rationale"
})
public class PlanningDto {
    @JsonProperty("rationale")
    @JsonPropertyDescription("The rationale for the procurement provided in free text. More detail can be provided in" +
        " an attached document.")
    @NotNull
    private final String rationale;

    @JsonProperty("budget")
    @JsonPropertyDescription("This section contain information about the budget line, and associated projects, " +
        "through which this contracting process is funded. It draws upon data model of the [Fiscal Data Package]" +
        "(http://fiscal.dataprotocols.org/), and should be used to cross-reference to more detailed information held " +
        "using a BudgetDto Data Package, or, where no linked BudgetDto Data Package is available, to provide enough " +
        "information to allow a user to manually or automatically cross-reference with another published source of " +
        "budget and project information.")
    @Valid
    @NotNull
    private final BudgetDto budget;


    @JsonCreator
    public PlanningDto(@JsonProperty("budget") final BudgetDto budget,
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
        if (!(other instanceof PlanningDto)) {
            return false;
        }
        final PlanningDto rhs = (PlanningDto) other;
        return new EqualsBuilder().append(rationale, rhs.rationale)
                                  .append(budget, rhs.budget)
                                  .isEquals();
    }
}
