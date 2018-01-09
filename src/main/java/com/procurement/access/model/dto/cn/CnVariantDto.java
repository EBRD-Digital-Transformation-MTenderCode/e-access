package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("hasVariants")
public class CnVariantDto {
    @JsonProperty("hasVariants")
    @JsonPropertyDescription("A True/False field to indicate if lot variants will be accepted. Required by the EU")
    @NotNull
    private final Boolean hasVariants;

    @JsonCreator
    public CnVariantDto(@JsonProperty("hasVariants") final Boolean hasVariants) {
        super();
        this.hasVariants = hasVariants;
    }
}
