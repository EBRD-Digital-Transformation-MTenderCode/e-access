package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
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
    private final CnPlanningDto planning;

    @JsonProperty("tender")
    @NotNull
    @Valid
    private final CnTenderDto tender;

    @JsonProperty("parties")
    @NotNull
    @Valid
    private final List<CnOrganizationDto> parties;

    @JsonProperty("buyer")
    @NotNull
    @Valid
    private final CnOrganizationDto buyer;

    @JsonProperty("relatedProcesses")
    @NotNull
    @Valid
    private final List<CnRelatedProcessDto> relatedProcesses;

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
