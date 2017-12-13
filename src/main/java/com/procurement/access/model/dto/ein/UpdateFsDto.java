package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
    "cpid",
    "relatedProcesses"
})
public class UpdateFsDto {

    @JsonProperty("cpid")
    private final String cpId;

    @JsonProperty("relatedProcesses")
    public final List<EinRelatedProcessDto> relatedProcesses;

    @JsonCreator
    public UpdateFsDto(@JsonProperty("cpid") final String cpId,
                       @JsonProperty("relatedProcesses") final List<EinRelatedProcessDto> relatedProcesses) {
        this.cpId = cpId;
        this.relatedProcesses = relatedProcesses;
    }
}
