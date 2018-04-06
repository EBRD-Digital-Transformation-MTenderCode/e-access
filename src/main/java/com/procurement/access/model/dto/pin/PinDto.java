package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.ocds.Tender;
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
public class PinDto {

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
    private PinTender tender;

    @JsonCreator
    public PinDto(@JsonProperty("token") final String token,
                  @JsonProperty("ocid") final String ocId,
                  @JsonProperty("planning") final Planning planning,
                  @JsonProperty("tender") final PinTender tender) {
        this.token = token;
        this.ocId = ocId;
        this.planning = planning;
        this.tender = tender;
    }
}
