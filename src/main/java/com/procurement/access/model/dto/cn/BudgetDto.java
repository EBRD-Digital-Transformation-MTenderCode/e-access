
package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "description",
    "amount",
    "isEuropeanUnionFunded",
    "budgetBreakdown",
})
public class BudgetDto {

    @JsonProperty("description")
    @JsonPropertyDescription("A short free text description of the budget source. May be used to provide the title of" +
        " the budget line, or the programme used to fund this project.")
    @Pattern(regexp = "^(description_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String description;

    @JsonProperty("amount")
    @Valid
    @NotNull
    private final ValueDto amount;

    @JsonProperty("isEuropeanUnionFunded")
    @JsonPropertyDescription("A True or False field to indicate whether this procurement is related to a project " +
        "and/or programme financed by European Union funds.")
    @NotNull
    private final Boolean isEuropeanUnionFunded;

    @JsonProperty("budgetBreakdown")
    @NotNull
    @JsonPropertyDescription("A detailed breakdown of the budget by period and/or participating funders.")
    @Valid
    private final List<BudgetBreakdownDto> budgetBreakdown;

    @JsonCreator
    public BudgetDto(
        @JsonProperty("description") final String description,
        @JsonProperty("amount") final ValueDto amount,
        @JsonProperty("isEuropeanUnionFunded") final Boolean isEuropeanUnionFunded,
        @JsonProperty("budgetBreakdown") final List<BudgetBreakdownDto> budgetBreakdown
    ) {

        this.description = description;
        this.amount = amount;
        this.isEuropeanUnionFunded = isEuropeanUnionFunded;
        this.budgetBreakdown = budgetBreakdown;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(description)
                                    .append(amount)
                                    .append(isEuropeanUnionFunded)
                                    .append(budgetBreakdown)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof BudgetDto)) {
            return false;
        }
        final BudgetDto rhs = (BudgetDto) other;
        return new EqualsBuilder().append(description, rhs.description)
                                  .append(amount, rhs.amount)
                                  .append(isEuropeanUnionFunded, rhs.isEuropeanUnionFunded)
                                  .append(budgetBreakdown, rhs.budgetBreakdown)
                                  .isEquals();
    }
}
