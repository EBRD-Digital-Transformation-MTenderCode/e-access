package com.ocds.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class EinDto {

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final PlanningDto planningDto;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private final TenderDto budgetDto;

    @JsonCreator
    public EinDto(@JsonProperty("planning")final PlanningDto planningDto,
                  @JsonProperty("budget") final TenderDto tenderDto) {
        this.planningDto = planningDto;
        this.budgetDto = tenderDto;
    }
}
