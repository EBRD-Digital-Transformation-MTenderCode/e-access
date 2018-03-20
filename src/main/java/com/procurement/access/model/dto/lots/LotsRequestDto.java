package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class LotsRequestDto {

    @JsonProperty(value = "unsuccessfulLots")
    private List<LotDto> lots;

    public LotsRequestDto(@JsonProperty("unsuccessfulLots") final List<LotDto> lots) {
        this.lots = lots;
    }

}
