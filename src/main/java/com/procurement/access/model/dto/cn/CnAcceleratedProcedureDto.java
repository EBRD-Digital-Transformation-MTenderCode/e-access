package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
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
