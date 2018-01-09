package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("name")
public class CnUnitDto {

    @JsonProperty("id")
    @JsonPropertyDescription("The identifier from the codelist referenced in the scheme property. Check the codelist " +
            "for details of how to find and use identifiers from the scheme in use.")
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("Name of the unit.")
    @NotNull
    private final String name;

    @JsonCreator
    public CnUnitDto(@JsonProperty("id") final String id,
                     @JsonProperty("name") final String name) {
        this.id = id;
        this.name = name;
    }
}
