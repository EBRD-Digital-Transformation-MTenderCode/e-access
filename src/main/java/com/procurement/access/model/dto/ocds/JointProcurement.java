package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class JointProcurement {

    @NotNull
    @JsonProperty("isJointProcurement")
    private final Boolean isJointProcurement;

    @JsonCreator
    public JointProcurement(@JsonProperty("isJointProcurement") final Boolean isJointProcurement) {
        this.isJointProcurement = isJointProcurement;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(isJointProcurement)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof JointProcurement)) {
            return false;
        }
        final JointProcurement rhs = (JointProcurement) other;
        return new EqualsBuilder()
                .append(isJointProcurement, rhs.isJointProcurement)
                .isEquals();
    }
}
