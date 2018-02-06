package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Organization;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({
        "ocid",
        "parties",
        "planning"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FsDto {
    @JsonProperty("ocid")
    private String ocId;
    @JsonProperty("planning")
    @NotNull
    @Valid
    private FsPlanningDto planning;
    @JsonProperty("parties")
    @NotNull
    @Valid
    private List<Organization> parties;

    @JsonCreator
    public FsDto(@JsonProperty("ocid") final String ocId,
                 @JsonProperty("planning") final FsPlanningDto planning,
                 @JsonProperty("parties") final List<Organization> parties) {
        this.ocId = ocId;
        this.planning = planning;
        this.parties = parties;
    }
}
