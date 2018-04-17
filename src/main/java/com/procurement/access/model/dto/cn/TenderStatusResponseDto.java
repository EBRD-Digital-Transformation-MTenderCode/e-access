package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "status",
        "statusDetails"
})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class TenderStatusResponseDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("statusDetails")
    private String statusDetails;

    @JsonCreator
    public TenderStatusResponseDto(@JsonProperty("status") final String status,
                                   @JsonProperty("statusDetails") final String statusDetails) {
        this.status = status;
        this.statusDetails = statusDetails;
    }
}
