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
    "planning",
    "parties",
    "language",
    "relatedProcesses"
})
public class FsDto {

    @JsonProperty("ocid")
    @NotNull
    private String ocid;

    @JsonProperty("id")
    @NotNull
    private String id;

    @JsonProperty("date")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @NotNull
    private LocalDateTime date;

    @JsonProperty("tag")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String tag;

    @JsonProperty("initiationType")
    @NotNull
    private String initiationType;

    @JsonProperty("planning")
    @NotNull
    @Valid
    private final FsPlanningDto planning;

    @JsonProperty("parties")
    @NotNull
    @Valid
    private final FsOrganizationDto parties;

    @JsonProperty("language")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String language;

    @JsonProperty("relatedProcesses")
    @NotNull
    @Valid
    private final List<FsRelatedProcessDto> relatedProcesses;



    @JsonCreator
    public FsDto(@JsonProperty("ocid") final String ocid,
                  @JsonProperty("id") final String id,
                  @JsonProperty("date") final LocalDateTime date,
                  @JsonProperty("tag") final String tag,
                  @JsonProperty("initiationType") final String initiationType,
                  @JsonProperty("planning") final FsPlanningDto planning,
                  @JsonProperty("parties") final FsOrganizationDto parties,
                  @JsonProperty("language") final String language,
                  @JsonProperty("relatedProcesses") final List<FsRelatedProcessDto> relatedProcesses) {
        this.ocid = ocid;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.planning = planning;
        this.parties = parties;
        this.language = language;
        this.relatedProcesses = relatedProcesses;
    }
}
