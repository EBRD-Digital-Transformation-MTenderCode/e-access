package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isRecurrent",
        "dates",
        "description"
})
public class RecurrentProcurement {
    @JsonProperty("isRecurrent")
    private final Boolean isRecurrent;

    @JsonProperty("dates")
    @Valid
    private final List<Period> dates;

    @JsonProperty("description")
    private final String description;

    @JsonCreator
    public RecurrentProcurement(@JsonProperty("isRecurrent") final Boolean isRecurrent,
                                @JsonProperty("dates") final List<Period> dates,
                                @JsonProperty("description") final String description) {
        this.isRecurrent = isRecurrent;
        this.dates = dates;
        this.description = description;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isRecurrent)
                .append(dates)
                .append(description)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RecurrentProcurement)) {
            return false;
        }
        final RecurrentProcurement rhs = (RecurrentProcurement) other;
        return new EqualsBuilder().append(isRecurrent, rhs.isRecurrent)
                .append(dates, rhs.dates)
                .append(description, rhs.description)
                .isEquals();
    }
}
