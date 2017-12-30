package com.procurement.access.model.dto.cn;

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
public class CnDto {

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final CnPlanningDto planning;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private final CnTenderDto tender;

    @JsonCreator
    public CnDto(@JsonProperty("planning") final CnPlanningDto planning,
                 @JsonProperty("budget") final CnTenderDto tender) {
        this.planning = planning;
        this.tender = tender;
    }
}
