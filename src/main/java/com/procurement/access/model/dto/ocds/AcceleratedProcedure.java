package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isAcceleratedProcedure",
        "acceleratedProcedureJustification"
})
public class AcceleratedProcedure {
    @JsonProperty("isAcceleratedProcedure")
    private final Boolean isAcceleratedProcedure;

    @JsonProperty("acceleratedProcedureJustification")
    private final String acceleratedProcedureJustification;

    @JsonCreator
    public AcceleratedProcedure(@JsonProperty("isAcceleratedProcedure") final Boolean isAcceleratedProcedure,
                                @JsonProperty("acceleratedProcedureJustification") final String
                                        acceleratedProcedureJustification) {
        this.isAcceleratedProcedure = isAcceleratedProcedure;
        this.acceleratedProcedureJustification = acceleratedProcedureJustification;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAcceleratedProcedure)
                .append(acceleratedProcedureJustification)
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
        final AcceleratedProcedure rhs = ((AcceleratedProcedure) other);
        return new EqualsBuilder().append(isAcceleratedProcedure, rhs.isAcceleratedProcedure)
                .append(acceleratedProcedureJustification, rhs.acceleratedProcedureJustification)
                .isEquals();
    }
}
