package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@JsonPropertyOrder({
        "startDate",
        "endDate",
        "durationInDays"
})
public class CnPeriodDto {
    @JsonProperty("startDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The start date for the period. When known, a precise start date must always be provided.")
    @NotNull
    private final LocalDateTime startDate;

    @JsonProperty("endDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The end date for the period. When known, a precise end date must always be provided.")
    @NotNull
    private final LocalDateTime endDate;

    @JsonProperty("durationInDays")
    @JsonPropertyDescription("The maximum duration of this period in days. A user interface may wish to collect or " +
            "display this data in months or years as appropriate, but should convert it into days when completing " +
            "this " +
            "field. This field can be used when exact dates are not known.  Where a startDate and endDate are given, " +
            "this" +
            " field is optional, and should reflect the difference between those two days. Where a startDate and " +
            "maxExtentDate are given, this field is optional, and should reflect the difference between startDate and" +
            " " +
            "maxExtentDate.")
    @NotNull
    private final Integer durationInDays;

    @JsonCreator
    public CnPeriodDto(@JsonProperty("startDate")
                       @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime startDate,
                       @JsonProperty("endDate")
                       @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime endDate,
                       @JsonProperty("durationInDays") final Integer durationInDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.durationInDays = durationInDays;
    }
}
