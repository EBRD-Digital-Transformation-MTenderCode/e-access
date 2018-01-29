package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class LotsResponseDto {

    @JsonProperty(value = "owner")
    private String owner;

    @JsonProperty(value = "lots")
    private List<LotDto> lots;

    public LotsResponseDto(@JsonProperty("id") final String owner,
                           @JsonProperty("lots") final List<LotDto> lots) {
        this.owner = owner;
        this.lots = lots;
    }

}
