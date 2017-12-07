package com.procurement.access.model.dto.pin;

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
public class PinDto {

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final PinPlanningDto planningDto;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private final PinTenderDto tenderDto;

    @JsonCreator
    public PinDto(@JsonProperty("planning")final PinPlanningDto planningDto,
                  @JsonProperty("budget") final PinTenderDto tenderDto) {
        this.planningDto = planningDto;
        this.tenderDto = tenderDto;
    }
}
