package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.procurement.access.model.dto.ocds.Lot;
import java.util.List;

public class LotsUpdateResponseDto {

    @JsonProperty(value = "lots")
    private List<Lot> lots;

    @JsonCreator
    public LotsUpdateResponseDto(@JsonProperty("lots") final List<Lot> lots) {
        this.lots = lots;
    }
}
