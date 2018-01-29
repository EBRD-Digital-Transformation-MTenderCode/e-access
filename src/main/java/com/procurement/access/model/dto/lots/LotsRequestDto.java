package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotsRequestDto {

    @JsonProperty(value = "lots")
    private List<LotDto> lots;

    public LotsRequestDto(@JsonProperty("lots") final List<LotDto> lots) {
        this.lots = lots;
    }

}
