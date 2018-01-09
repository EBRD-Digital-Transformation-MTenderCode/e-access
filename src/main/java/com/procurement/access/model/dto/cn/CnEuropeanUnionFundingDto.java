package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "projectIdentifier",
        "projectName",
        "uri"
})
public class CnEuropeanUnionFundingDto {
    @JsonProperty("projectIdentifier")
    @JsonPropertyDescription("National identifier of the EU project providing partial or full funding")
    private final String projectIdentifier;

    @JsonProperty("projectName")
    @JsonPropertyDescription("Name or other national identification of the project providing full or partial funding.")
    private final String projectName;

    @JsonProperty("uri")
    @JsonPropertyDescription("Uri of the project providing full or partial funding.")
    private final String uri;

    @JsonCreator
    public CnEuropeanUnionFundingDto(@JsonProperty("projectIdentifier") final String projectIdentifier,
                                     @JsonProperty("projectName") final String projectName,
                                     @JsonProperty("uri") final String uri) {
        this.projectIdentifier = projectIdentifier;
        this.projectName = projectName;
        this.uri = uri;
    }
}
