package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("hasDynamicPurchasingSystem")
public class CnDynamicPurchasingSystemDto {
    @JsonProperty("hasDynamicPurchasingSystem")
    @JsonPropertyDescription("A True/False field to indicate whether a Dynamic Purchasing System has been set up.")
    @NotNull
    private final Boolean hasDynamicPurchasingSystem;

    @JsonCreator
    public CnDynamicPurchasingSystemDto(@JsonProperty("hasDynamicPurchasingSystem") final Boolean
                                                hasDynamicPurchasingSystem) {
        this.hasDynamicPurchasingSystem = hasDynamicPurchasingSystem;
    }
}
