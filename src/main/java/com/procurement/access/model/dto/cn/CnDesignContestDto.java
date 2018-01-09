package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("serviceContractAward")
public class CnDesignContestDto {

    @JsonProperty("serviceContractAward")
    @JsonPropertyDescription("A True/False field to indicate whether a service contract will be awarded to the winner" +
            "(s) of the design contest. Required by the EU")
    @NotNull
    private final Boolean serviceContractAward;

    @JsonCreator
    public CnDesignContestDto(
            @JsonProperty("serviceContractAward") final Boolean serviceContractAward) {

        this.serviceContractAward = serviceContractAward;
    }
}
