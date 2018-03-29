package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class RecurrentProcurement {

    @NotNull
    @JsonProperty("isRecurrent")
    private final Boolean isRecurrent;

    @JsonCreator
    public RecurrentProcurement(@JsonProperty("isRecurrent") final Boolean isRecurrent) {
        this.isRecurrent = isRecurrent;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isRecurrent)
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
        return new EqualsBuilder()
                .append(isRecurrent, rhs.isRecurrent)
                .isEquals();
    }
}
