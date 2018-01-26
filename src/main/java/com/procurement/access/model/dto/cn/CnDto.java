package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "planning",
        "tender",
        "parties",
        "buyer",
        "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CnDto {

    @JsonProperty("planning")
    @NotNull
    @Valid
    private CnPlanningDto planning;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private CnTenderDto tender;

    @JsonProperty("parties")
    @NotNull
    @Valid
    private List<CnOrganizationDto> parties;

    @JsonProperty("buyer")
    @NotNull
    @Valid
    private CnOrganizationDto buyer;

    @JsonProperty("relatedProcesses")
    @NotNull
    @Valid
    private List<CnRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public CnDto(@JsonProperty("planning") final CnPlanningDto planning,
                 @JsonProperty("tender") final CnTenderDto tender,
                 @JsonProperty("parties") final List<CnOrganizationDto> parties,
                 @JsonProperty("buyer") final CnOrganizationDto buyer,
                 @JsonProperty("relatedProcesses") final List<CnRelatedProcessDto> relatedProcesses) {
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.relatedProcesses = relatedProcesses;
    }
}
