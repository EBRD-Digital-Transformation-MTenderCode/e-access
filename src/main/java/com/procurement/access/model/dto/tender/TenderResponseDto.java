package com.procurement.access.model.dto.tender;

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
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "token",
        "ocid",
        "date",
        "planning",
        "tender",
        "parties",
        "buyer"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TenderResponseDto {

    @JsonProperty("token")
    private final String token;

    @JsonProperty("ocid")
    private final String ocid;

    @JsonProperty("date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;

    @JsonProperty("planning")
    private final Planning planning;

    @JsonProperty("tender")
    private final Tender tender;

    @JsonProperty("parties")
    private final List<Organization> parties;

    @JsonProperty("buyer")
    private final OrganizationReference buyer;

    @JsonCreator
    public TenderResponseDto(@JsonProperty("token") final String token,
                             @JsonProperty("ocid") final String ocid,
                             @JsonProperty("date") final LocalDateTime date,
                             @JsonProperty("planning") final Planning planning,
                             @JsonProperty("tender") final Tender tender,
                             @JsonProperty("parties") final List<Organization> parties,
                             @JsonProperty("buyer") final OrganizationReference buyer) {
        this.ocid = ocid;
        this.token = token;
        this.date = date;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
    }
}
