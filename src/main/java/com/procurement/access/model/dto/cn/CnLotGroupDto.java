package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("optionToCombine")
public class CnLotGroupDto {

    @JsonProperty("optionToCombine")
    @JsonPropertyDescription("The buyer reserves the right to combine the lots in this group when awarding a contract.")
    @NotNull
    private final Boolean optionToCombine;

    @JsonCreator
    public CnLotGroupDto(
            @JsonProperty("optionToCombine") final Boolean optionToCombine) {

        this.optionToCombine = optionToCombine;
    }
}
