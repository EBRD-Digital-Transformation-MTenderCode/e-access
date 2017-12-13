package com.procurement.access.model.dto.fs;

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
    "parties",
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
    private List<String> tag;

    @JsonProperty("initiationType")
    private String initiationType;

    @JsonProperty("language")
    private String language;

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final FsPlanningDto planning;

    @JsonProperty("parties")
    @NotNull
    @Valid
    private final FsOrganizationDto parties;

    @JsonProperty("relatedProcesses")
    @NotNull
    @Valid
    private final List<FsRelatedProcessDto> relatedProcesses;



    @JsonCreator
    public FsDto(@JsonProperty("ocid") final String ocId,
                  @JsonProperty("id") final String id,
                  @JsonProperty("date") final LocalDateTime date,
                  @JsonProperty("tag") final List<String> tag,
                  @JsonProperty("initiationType") final String initiationType,
                  @JsonProperty("language") final String language,
                  @JsonProperty("planning") final FsPlanningDto planning,
                  @JsonProperty("parties") final FsOrganizationDto parties,
                  @JsonProperty("relatedProcesses") final List<FsRelatedProcessDto> relatedProcesses) {
        this.ocId = ocId;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.language = language;
        this.planning = planning;
        this.parties = parties;
        this.relatedProcesses = relatedProcesses;
    }
}
