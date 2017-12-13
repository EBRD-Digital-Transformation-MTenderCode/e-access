package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
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
    "id",
    "date",
    "tag",
    "initiationType",
    "language",
    "planning",
    "tender",
    "parties",
    "buyer",
    "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class EinDto {

    @JsonProperty("ocid")
    private String ocId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;

    @JsonProperty("tag")
    private List<String> tag;

    @JsonProperty("initiationType")
    private String initiationType;

    @JsonProperty("language")
    private String language;

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

    @JsonProperty("relatedProcesses")
    public final List<EinRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public EinDto(@JsonProperty("ocid") final String ocId,
                  @JsonProperty("id") final String id,
                  @JsonProperty("date") final LocalDateTime date,
                  @JsonProperty("tag") final List<String> tag,
                  @JsonProperty("initiationType") final String initiationType,
                  @JsonProperty("language") final String language,
                  @JsonProperty("planning") final EinPlanningDto planning,
                  @JsonProperty("tender") final EinTenderDto tender,
                  @JsonProperty("parties") final List<EinOrganizationDto> parties,
                  @JsonProperty("buyer") final EinOrganizationReferenceDto buyer,
                  @JsonProperty("relatedProcesses") final List<EinRelatedProcessDto> relatedProcesses) {
        this.ocId = ocId;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.language = language;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.relatedProcesses = relatedProcesses;
    }
}
