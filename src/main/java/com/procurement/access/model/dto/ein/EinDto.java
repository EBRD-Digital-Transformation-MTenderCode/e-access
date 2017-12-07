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
    "planning",
    "tender",
    "parties",
    "buyer",
    "language",
    "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class EinDto {

    @JsonProperty("ocid")
    private String ocid;

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

    @NotNull
    @Valid
    @JsonProperty("language")
    private final String language;

    @JsonProperty("relatedProcesses")
    public final List<EinRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public EinDto(@JsonProperty("ocid") String ocid,
                  @JsonProperty("id") String id,
                  @JsonProperty("date") LocalDateTime date,
                  @JsonProperty("tag") List<String> tag,
                  @JsonProperty("initiationType") String initiationType,
                  @JsonProperty("planning") EinPlanningDto planning,
                  @JsonProperty("tender") EinTenderDto tender,
                  @JsonProperty("parties") List<EinOrganizationDto> parties,
                  @JsonProperty("buyer") EinOrganizationReferenceDto buyer,
                  @JsonProperty("language") String language,
                  @JsonProperty("relatedProcesses") List<EinRelatedProcessDto> relatedProcesses) {
        this.ocid = ocid;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.planning = planning;
        this.tender = tender;
        this.parties = parties;
        this.buyer = buyer;
        this.language = language;
        this.relatedProcesses = relatedProcesses;
    }
}
