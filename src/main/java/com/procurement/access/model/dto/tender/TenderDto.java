package com.procurement.access.model.dto.tender;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import com.procurement.access.model.dto.ocds.Organization;
import com.procurement.access.model.dto.ocds.Planning;
import com.procurement.access.model.dto.ocds.RelatedProcess;
import com.procurement.access.model.dto.ocds.Tender;
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
        "date",
        "planning",
        "tender",
        "parties",
        "buyer"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TenderDto {
    @JsonProperty("ocid")
    private String ocId;
    @JsonProperty("date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    @JsonProperty("planning")
    @NotNull
    @Valid
    private Planning planning;

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
    private Organization buyer;

    @JsonCreator
    public TenderDto(@JsonProperty("ocid") final String ocId,
                     @JsonProperty("date") final LocalDateTime date,
                     @JsonProperty("planning") final Planning planning,
                     @JsonProperty("tender") final Tender tender,
                     @JsonProperty("parties") final List<Organization> parties,
                     @JsonProperty("buyer") final Organization buyer) {
        this.ocId = ocId;
        this.date = date;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
    }
}
