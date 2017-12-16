package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
    "cpid",
    "ocid"
})
public class UpdateFsDto {

    @JsonProperty("cpid")
    @NotNull
    private final String cpId;

    @JsonProperty("ocid")
    @NotNull
    private final String ocId;

    @JsonCreator
    public UpdateFsDto(@JsonProperty("cpid") final String cpId,
                       @JsonProperty("ocid") final String ocId) {
        this.cpId = cpId;
        this.ocId = ocId;
    }
}
