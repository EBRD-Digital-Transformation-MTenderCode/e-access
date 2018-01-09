package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@JsonPropertyOrder("isAcceleratedProcedure")
public class CnAcceleratedProcedureDto {
    @JsonProperty("isAcceleratedProcedure")
    @JsonPropertyDescription("A True/False field to indicate whether an accelerated procedure has been used for this " +
            "procurement")
    @NotNull
    private final Boolean isAcceleratedProcedure;

    @JsonCreator
    public CnAcceleratedProcedureDto(@JsonProperty("isAcceleratedProcedure") final Boolean isAcceleratedProcedure
    ) {
        this.isAcceleratedProcedure = isAcceleratedProcedure;
    }
}
