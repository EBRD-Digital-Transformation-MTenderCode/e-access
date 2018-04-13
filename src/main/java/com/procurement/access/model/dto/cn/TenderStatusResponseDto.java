package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import com.procurement.access.model.dto.ocds.*;
import java.time.LocalDateTime;
import java.util.List;
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
