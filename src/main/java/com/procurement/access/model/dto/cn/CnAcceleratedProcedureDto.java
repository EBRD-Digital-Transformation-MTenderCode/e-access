
package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder("isAcceleratedProcedure")
public class CnAcceleratedProcedureDto {
    @JsonProperty("isAcceleratedProcedure")
    @JsonPropertyDescription("A True/False field to indicate whether an accelerated procedure has been used for this " +
        "procurement")
    @NotNull
    private final Boolean isAcceleratedProcedure;

    @JsonCreator
    public CnAcceleratedProcedureDto(@JsonProperty("isAcceleratedProcedure") final Boolean isAcceleratedProcedure
    ) {
        this.isAcceleratedProcedure = isAcceleratedProcedure;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isAcceleratedProcedure)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnAcceleratedProcedureDto)) {
            return false;
        }
        final CnAcceleratedProcedureDto rhs = (CnAcceleratedProcedureDto) other;
        return new EqualsBuilder().append(isAcceleratedProcedure, rhs.isAcceleratedProcedure)
                                  .isEquals();
    }
}
