package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "startDate",
        "endDate",
        "maxExtentDate",
        "durationInDays"
})
public class Period {
    @JsonProperty("startDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime startDate;

    @JsonProperty("endDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime endDate;

    @JsonProperty("maxExtentDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime maxExtentDate;

    @JsonProperty("durationInDays")
    private final Integer durationInDays;

    @JsonCreator
    public Period(@JsonProperty("startDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime startDate,
                  @JsonProperty("endDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime endDate,
                  @JsonProperty("maxExtentDate") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime maxExtentDate,
                  @JsonProperty("durationInDays") final Integer durationInDays) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxExtentDate = maxExtentDate;
        this.durationInDays = durationInDays;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(startDate)
                .append(endDate)
                .append(maxExtentDate)
                .append(durationInDays)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Period)) {
            return false;
        }
        final Period rhs = (Period) other;
        return new EqualsBuilder().append(startDate, rhs.startDate)
                .append(endDate, rhs.endDate)
                .append(maxExtentDate, rhs.maxExtentDate)
                .append(durationInDays, rhs.durationInDays)
                .isEquals();
    }
}
