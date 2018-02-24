package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class LotsRequestDto {

    @JsonProperty(value = "lots")
    private List<LotDto> lots;

    public LotsRequestDto(@JsonProperty("lots") final List<LotDto> lots) {
        this.lots = lots;
    }

}
