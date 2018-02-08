package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "budget",
        "rationale"
})
public class EinPlanningDto {
    @JsonProperty("budget")
    @Valid
    private final EinBudgetDto budget;
    @JsonProperty("rationale")
    private final String rationale;

    @JsonCreator
    public EinPlanningDto(@JsonProperty("budget") final EinBudgetDto budget,
                          @JsonProperty("rationale") final String rationale) {
        this.budget = budget;
        this.rationale = rationale;
    }
}
