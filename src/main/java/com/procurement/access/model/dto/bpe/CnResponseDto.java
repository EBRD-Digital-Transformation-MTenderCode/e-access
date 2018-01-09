package com.procurement.access.model.dto.bpe;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.cn.CnOrganizationDto;
import com.procurement.access.model.dto.cn.CnPlanningDto;
import com.procurement.access.model.dto.cn.CnRelatedProcessDto;
import com.procurement.access.model.dto.cn.CnTenderDto;
import java.util.List;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "ocid",
        "token",
        "planning",
        "tender",
        "parties",
        "buyer",
        "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CnResponseDto {

    @JsonProperty("ocid")
    private final String ocid;

    @JsonProperty("token")
    private final String token;

    @JsonProperty("planning")
    private final CnPlanningDto planning;

    @JsonProperty("tender")
    private final CnTenderDto tender;

    @JsonProperty("parties")
    private final List<CnOrganizationDto> parties;

    @JsonProperty("buyer")
    private final CnOrganizationDto buyer;

    @JsonProperty("relatedProcesses")
    private final List<CnRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public CnResponseDto(@JsonProperty("ocid") final String ocid,
                         @JsonProperty("token") final String token,
                         @JsonProperty("planning") final CnPlanningDto planning,
                         @JsonProperty("tender") final CnTenderDto tender,
                         @JsonProperty("parties") final List<CnOrganizationDto> parties,
                         @JsonProperty("buyer") final CnOrganizationDto buyer,
                         @JsonProperty("relatedProcesses") final List<CnRelatedProcessDto> relatedProcesses) {
        this.ocid = ocid;
        this.token = token;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.relatedProcesses = relatedProcesses;
    }
}
