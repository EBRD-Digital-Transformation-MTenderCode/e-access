package com.procurement.access.model.dto.ein;

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
    "cpid",
    "planning",
    "tender",
    "parties",
    "buyer",
    "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class EinDto {
    @JsonProperty("relatedProcesses")
    private final List<EinRelatedProcessDto> relatedProcesses;
    @JsonProperty("planning")
    @NotNull
    @Valid
    private final EinPlanningDto planning;
    @JsonProperty("tender")
    @NotNull
    @Valid
    private final EinTenderDto tender;
    @JsonProperty("parties")
    @NotNull
    @Valid
    private final List<EinOrganizationDto> parties;
    @JsonProperty("buyer")
    @NotNull
    @Valid
    private final EinOrganizationReferenceDto buyer;

    @JsonProperty("cpid")
    private String cpId;

    @JsonCreator
    public EinDto(
        @JsonProperty("cpid") final String cpId,
        @JsonProperty("planning") final EinPlanningDto planning,
        @JsonProperty("tender") final EinTenderDto tender,
        @JsonProperty("parties") final List<EinOrganizationDto> parties,
        @JsonProperty("buyer") final EinOrganizationReferenceDto buyer,
        @JsonProperty("relatedProcesses") final List<EinRelatedProcessDto> relatedProcesses) {
        this.cpId = cpId;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.relatedProcesses = relatedProcesses;
    }
}
