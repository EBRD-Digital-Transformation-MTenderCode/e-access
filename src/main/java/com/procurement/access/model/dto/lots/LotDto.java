package com.procurement.access.model.dto.lots;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LotDto {

    @JsonProperty(value = "id")
    private String id;

    public LotDto(@JsonProperty("id") final String id) {
        this.id = id;
    }
}
