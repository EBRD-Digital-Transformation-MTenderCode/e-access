package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import com.procurement.access.model.dto.enums.InitiationType;
import com.procurement.access.model.dto.enums.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "token",
        "cpid",
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
public class FsResponseDto {
    @JsonProperty("token")
    private final String token;
    @JsonProperty("cpid")
    private String cpId;
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
    @JsonProperty("planning")
    private final FsPlanningDto planning;
    @JsonProperty("parties")
    private final List<FsOrganizationDto> parties;
    @JsonProperty("relatedProcesses")
    private List<FsRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public FsResponseDto(@JsonProperty("token") final String token,
                         @JsonProperty("cpid") final String cpId,
                         @JsonProperty("ocid") final String ocId,
                         @JsonProperty("id") final String id,
                         @JsonProperty("date") final LocalDateTime date,
                         @JsonProperty("tag") final List<Tag> tag,
                         @JsonProperty("initiationType") final InitiationType initiationType,
                         @JsonProperty("language") final String language,
                         @JsonProperty("planning") final FsPlanningDto planning,
                         @JsonProperty("parties") final List<FsOrganizationDto> parties,
                         @JsonProperty("relatedProcesses") final List<FsRelatedProcessDto> relatedProcesses) {
        this.token = token;
        this.cpId = cpId;
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
