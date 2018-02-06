package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import com.procurement.access.model.dto.ocds.*;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "ocid",
        "planning",
        "tender",
        "parties",
        "buyer"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class EinDto {
    @JsonProperty("ocid")
    private String ocId;
    @NotNull
    @Valid
    private EinPlanningDto planning;
    @JsonProperty("tender")
    @NotNull
    @Valid
    private Tender tender;
    @JsonProperty("parties")
    @NotNull
    @Valid
    private List<Organization> parties;
    @JsonProperty("buyer")
    @NotNull
    @Valid
    private OrganizationReference buyer;

    @JsonCreator
    public EinDto(@JsonProperty("ocid") final String ocId,
                  @JsonProperty("planning") final EinPlanningDto planning,
                  @JsonProperty("tender") final Tender tender,
                  @JsonProperty("parties") final List<Organization> parties,
                  @JsonProperty("buyer") final OrganizationReference buyer) {
        this.ocId = ocId;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
    }
}
