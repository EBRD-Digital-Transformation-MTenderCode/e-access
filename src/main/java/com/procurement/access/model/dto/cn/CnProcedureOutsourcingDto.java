package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("procedureOutsourced")
public class CnProcedureOutsourcingDto {
    @JsonProperty("procedureOutsourced")
    @JsonPropertyDescription("A True/False field to indicate whether the procurement procedure has been outsourced")
    @NotNull
    private final Boolean procedureOutsourced;

    @JsonCreator
    public CnProcedureOutsourcingDto(@JsonProperty("procedureOutsourced") final Boolean procedureOutsourced) {
        this.procedureOutsourced = procedureOutsourced;
    }
}
