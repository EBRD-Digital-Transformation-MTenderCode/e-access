package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.ocds.Tender;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
    "planning",
    "tender"
})
public class PnToPinDto {

    @Valid
    @NotNull
    @JsonProperty("planning")
    private Planning planning;

    @Valid
    @NotNull
    @JsonProperty("tender")
    private PnToPinTender tender;

    @JsonCreator
    public PnToPinDto(@JsonProperty("tender") final PnToPinTender tender,
                      @JsonProperty("plannung") final Planning planning) {
        this.tender = tender;
        this.planning=planning;
    }
}
