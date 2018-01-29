
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
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
    @JsonPropertyDescription("A True/False field to indicate whether this is a recurrent procurement")
    private final Boolean isRecurrent;

    @JsonProperty("dates")
    @JsonPropertyDescription("Estimated date(s) for subsequent call(s) for competition")
    @Valid
    private final List<Period> dates;

    @JsonProperty("description")
    @JsonPropertyDescription("Any further information about subsequent call(s) for competition.")
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
