
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
@JsonPropertyOrder
    ("isJointProcurement")
public class CnJointProcurementDto {
    @JsonProperty("isJointProcurement")
    @JsonPropertyDescription("A True/False field to indicate if this is a joint procurement or not. Required by the EU")
    @NotNull
    private final Boolean isJointProcurement;

    @JsonCreator
    public CnJointProcurementDto(@JsonProperty("isJointProcurement") final Boolean isJointProcurement) {
        this.isJointProcurement = isJointProcurement;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isJointProcurement)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnJointProcurementDto)) {
            return false;
        }
        final CnJointProcurementDto rhs = (CnJointProcurementDto) other;
        return new EqualsBuilder().append(isJointProcurement, rhs.isJointProcurement)
                                  .isEquals();
    }
}
