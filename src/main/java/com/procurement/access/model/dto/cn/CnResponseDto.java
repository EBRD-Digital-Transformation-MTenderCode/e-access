package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Organization;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.ocds.RelatedProcess;
import com.procurement.access.model.dto.ocds.Tender;
import java.util.List;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "token",
        "ocid",
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
    private final Planning planning;

    @JsonProperty("tender")
    private final Tender tender;

    @JsonProperty("parties")
    private final List<Organization> parties;

    @JsonProperty("buyer")
    private final Organization buyer;

    @JsonProperty("relatedProcesses")
    private final List<RelatedProcess> relatedProcesses;

    @JsonCreator
    public CnResponseDto(@JsonProperty("token") final String token,
                         @JsonProperty("ocid") final String ocid,
                         @JsonProperty("planning") final Planning planning,
                         @JsonProperty("tender") final Tender tender,
                         @JsonProperty("parties") final List<Organization> parties,
                         @JsonProperty("buyer") final Organization buyer,
                         @JsonProperty("relatedProcesses") final List<RelatedProcess> relatedProcesses) {
        this.ocid = ocid;
        this.token = token;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.relatedProcesses = relatedProcesses;
    }
}
