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
    "budget",
    "rationale"
})
public class EinPlanningDto {
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
    private final EinBudgetDto budget;


    @JsonCreator
    public EinPlanningDto(@JsonProperty("budget") final EinBudgetDto budget,
                          @JsonProperty("rationale") final String rationale) {
        this.budget = budget;
        this.rationale = rationale;
    }
}
