package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder("isAFramework")
public class CnFrameworkDto {
    @JsonProperty("isAFramework")
    @JsonPropertyDescription("A True/False field to indicate whether a framework agreement has been established as " +
            "part of this procurement")
    @NotNull
    private final Boolean isAFramework;

    @JsonCreator
    public CnFrameworkDto(@JsonProperty("isAFramework") final Boolean isAFramework) {
        this.isAFramework = isAFramework;
    }
}
