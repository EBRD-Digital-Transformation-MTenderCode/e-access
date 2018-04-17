package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.pn.PnTender;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "token",
        "ocid",
        "planning",
        "tender"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CnProcess {

    @JsonProperty("token")
    private String token;

    @JsonProperty("ocid")
    private String ocId;

    @Valid
    @NotNull
    @JsonProperty("planning")
    private Planning planning;

    @Valid
    @NotNull
    @JsonProperty("tender")
    private CnTender tender;

    @JsonCreator
    public CnProcess(@JsonProperty("token") final String token,
                     @JsonProperty("ocid") final String ocId,
                     @JsonProperty("planning") final Planning planning,
                     @JsonProperty("tender") final CnTender tender) {
        this.token = token;
        this.ocId = ocId;
        this.planning = planning;
        this.tender = tender;
    }
}
