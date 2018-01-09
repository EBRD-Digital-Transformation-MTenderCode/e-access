package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder
        ("isJointProcurement")
public class CnJointProcurementDto {
    @JsonProperty("isJointProcurement")
    @JsonPropertyDescription("A True/False field to indicate if this is a joint procurement or not. Required by the EU")
    @NotNull
    private final Boolean isJointProcurement;

    @JsonCreator
    public CnJointProcurementDto(@JsonProperty("isJointProcurement") final Boolean isJointProcurement) {
        this.isJointProcurement = isJointProcurement;
    }
}
