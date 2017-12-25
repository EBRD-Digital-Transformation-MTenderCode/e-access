package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Setter;

@Setter
public class EinResponseDto {

    @JsonProperty(value = "cpid")
    private String cpId;

    @JsonProperty(value = "ocid")
    private String ocId;

    @JsonProperty(value = "jsonData")
    private EinDto jsonData;
}
