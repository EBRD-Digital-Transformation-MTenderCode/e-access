package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("hasRenewals")
public class CnRenewalDto {
    @JsonProperty("hasRenewals")
    @JsonPropertyDescription("A True/False field to indicate whether contract renewals are allowed.")
    @NotNull
    private final Boolean hasRenewals;

    @JsonCreator
    public CnRenewalDto(@JsonProperty("hasRenewals") final Boolean hasRenewals) {
        this.hasRenewals = hasRenewals;
    }
}
