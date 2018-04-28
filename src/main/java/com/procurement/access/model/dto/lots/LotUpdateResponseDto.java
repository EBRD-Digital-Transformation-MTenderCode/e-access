package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.procurement.access.model.dto.ocds.Lot;

public class LotUpdateResponseDto {

    @JsonProperty(value = "lot")
    private Lot lot;

    @JsonCreator
    public LotUpdateResponseDto(@JsonProperty("lot") final Lot lot) {
        this.lot = lot;
    }
}
