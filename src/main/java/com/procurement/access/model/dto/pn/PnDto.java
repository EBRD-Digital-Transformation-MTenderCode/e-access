package com.procurement.access.model.dto.pn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "planning",
        "tender"
})
public class PnDto {

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final PnPlanningDto planningDto;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private final PnTenderDto tenderDto;

    @JsonCreator
    public PnDto(@JsonProperty("planning") final PnPlanningDto planningDto,
                 @JsonProperty("budget") final PnTenderDto tenderDto) {
        this.planningDto = planningDto;
        this.tenderDto = tenderDto;
    }
}
