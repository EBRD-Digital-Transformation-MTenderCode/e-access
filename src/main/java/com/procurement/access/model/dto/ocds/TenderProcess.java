package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
public class TenderProcess {

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
    private Tender tender;

    @JsonCreator
    public TenderProcess(@JsonProperty("token") final String token,
                         @JsonProperty("ocid") final String ocId,
                         @JsonProperty("planning") final Planning planning,
                         @JsonProperty("tender") final Tender tender) {
        this.token = token;
        this.ocId = ocId;
        this.planning = planning;
        this.tender = tender;
    }
}
