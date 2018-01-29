
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
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
    @JsonPropertyDescription("A True/False field to indicate whether an accelerated procedure has been used for this " +
        "procurement")
    private final Boolean isAcceleratedProcedure;

    @JsonProperty("acceleratedProcedureJustification")
    @JsonPropertyDescription("Justification for using an accelerated procedure")
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
