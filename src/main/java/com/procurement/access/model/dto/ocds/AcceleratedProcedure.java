package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class AcceleratedProcedure {

    @NotNull
    @JsonProperty("isAcceleratedProcedure")
    private final Boolean isAcceleratedProcedure;

    @JsonCreator
    public AcceleratedProcedure(@JsonProperty("isAcceleratedProcedure") final Boolean isAcceleratedProcedure) {
        this.isAcceleratedProcedure = isAcceleratedProcedure;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isAcceleratedProcedure)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AcceleratedProcedure)) {
            return false;
        }
        final AcceleratedProcedure rhs = (AcceleratedProcedure) other;
        return new EqualsBuilder()
                .append(isAcceleratedProcedure, rhs.isAcceleratedProcedure)
                .isEquals();
    }
}
