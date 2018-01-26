package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import com.procurement.access.model.dto.ein.EinTenderDto;
import com.procurement.access.model.dto.enums.InitiationType;
import com.procurement.access.model.dto.enums.Tag;
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
        "tender",
        "parties",
        "planning",
        "relatedProcesses"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FsDto {
    @JsonProperty("ocid")
    private String ocId;
    @JsonProperty("id")
    private String id;
    @JsonProperty("date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime date;
    @JsonProperty("tag")
    private List<Tag> tag;
    @JsonProperty("initiationType")
    private InitiationType initiationType;
    @JsonProperty("language")
    private String language;
    @JsonProperty("tender")
    @Valid
    private FsTenderDto tender;
    @JsonProperty("planning")
    @NotNull
    @Valid
    private FsPlanningDto planning;
    @JsonProperty("parties")
    @NotNull
    @Valid
    private List<FsOrganizationDto> parties;
    @JsonProperty("relatedProcesses")
    @NotNull
    @Valid
    private List<FsRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public FsDto(@JsonProperty("ocid") final String ocId,
                 @JsonProperty("id") final String id,
                 @JsonProperty("date") final LocalDateTime date,
                 @JsonProperty("tag") final List<Tag> tag,
                 @JsonProperty("initiationType") final InitiationType initiationType,
                 @JsonProperty("language") final String language,
                 @JsonProperty("tender") final FsTenderDto tender,
                 @JsonProperty("planning") final FsPlanningDto planning,
                 @JsonProperty("parties") final List<FsOrganizationDto> parties,
                 @JsonProperty("relatedProcesses") final List<FsRelatedProcessDto> relatedProcesses) {
        this.ocId = ocId;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.language = language == null ? "en" : language;
        this.tender = tender;
        this.planning = planning;
        this.parties = parties;
        this.relatedProcesses = relatedProcesses;
    }
}
