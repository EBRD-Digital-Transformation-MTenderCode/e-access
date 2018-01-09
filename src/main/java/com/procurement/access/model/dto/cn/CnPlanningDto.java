package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("budget")
public class CnPlanningDto {

    @JsonProperty("rationale")
    @JsonPropertyDescription("The rationale for the procurement provided in free text. More detail can be provided in" +
            " an attached document.")
    private final String rationale;

    @JsonProperty("budget")
    @JsonPropertyDescription("This section contain information about the budget line, and associated projects, " +
            "through which this contracting process is funded. It draws upon data model of the [Fiscal Data Package]" +
            "(http://fiscal.dataprotocols.org/), and should be used to cross-reference to more detailed information " +
            "held " +
            "using a CnBudgetDto Data Package, or, where no linked CnBudgetDto Data Package is available, to provide " +
            "enough " +
            "information to allow a user to manually or automatically cross-reference with another published source " +
            "of " +
            "budget and project information.")
    @Valid
    @NotNull
    private final CnBudgetDto budget;

    @JsonCreator
    public CnPlanningDto(@JsonProperty("rationale") final String rationale,
                         @JsonProperty("budget") final CnBudgetDto budget) {
        this.rationale = rationale;
        this.budget = budget;
    }
}
