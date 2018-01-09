package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder("hasOptions")
public class CnOptionDto {
    @JsonProperty("hasOptions")
    @JsonPropertyDescription("A True/False field to indicate if lot options will be accepted. Required by the EU")
    private final Boolean hasOptions;

    @JsonCreator
    public CnOptionDto(@JsonProperty("hasOptions") final Boolean hasOptions) {
        this.hasOptions = hasOptions;
    }
}
