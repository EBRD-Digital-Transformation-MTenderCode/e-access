
package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "isJointProcurement"
})
public class JointProcurementDto {
    @JsonProperty("isJointProcurement")
    @JsonPropertyDescription("A True/False field to indicate if this is a joint procurement or not. Required by the EU")
    private final Boolean isJointProcurement;

    @JsonCreator
    public JointProcurementDto(@JsonProperty("isJointProcurement") final Boolean isJointProcurement) {
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
        if (!(other instanceof JointProcurementDto)) {
            return false;
        }
        final JointProcurementDto rhs = (JointProcurementDto) other;
        return new EqualsBuilder().append(isJointProcurement, rhs.isJointProcurement)
                                  .isEquals();
    }
}
