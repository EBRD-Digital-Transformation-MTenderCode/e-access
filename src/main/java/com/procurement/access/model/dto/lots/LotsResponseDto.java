package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class LotsResponseDto {

    @JsonProperty(value = "awardCriteria")
    private String awardCriteria;

    @JsonProperty(value = "lots")
    private List<LotDto> lots;

    @JsonCreator
    public LotsResponseDto(@JsonProperty("awardCriteria") final String awardCriteria,
                           @JsonProperty("lots") final List<LotDto> lots) {
        this.awardCriteria = awardCriteria;
        this.lots = lots;
    }

}
