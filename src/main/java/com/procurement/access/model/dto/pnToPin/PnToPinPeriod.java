package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("startDate")
public class PnToPinPeriod {

    @NotNull
    @JsonProperty("startDate")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private final LocalDateTime startDate;



    @JsonCreator
    public PnToPinPeriod(@JsonProperty("startDate") final LocalDateTime startDate) {
        this.startDate = startDate;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(startDate)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnToPinPeriod)) {
            return false;
        }
        final PnToPinPeriod rhs = (PnToPinPeriod) other;
        return new EqualsBuilder()
                .append(startDate, rhs.startDate)
                .isEquals();
    }
}
