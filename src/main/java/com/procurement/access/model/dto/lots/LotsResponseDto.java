package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotsResponseDto {

    @JsonProperty(value = "owner")
    private String owner;

    @JsonProperty(value = "lots")
    private List<LotDto> lots;

    @JsonCreator
    public LotsResponseDto(@JsonProperty("id") final String owner,
                           @JsonProperty("lots") final List<LotDto> lots) {
        this.owner = owner;
        this.lots = lots;
    }

}
