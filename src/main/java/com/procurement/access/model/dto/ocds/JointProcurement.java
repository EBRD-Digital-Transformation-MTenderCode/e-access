package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isJointProcurement",
        "country"
})
public class JointProcurement {
    @JsonProperty("isJointProcurement")
    private final Boolean isJointProcurement;

    @JsonProperty("country")
    private final String country;

    @JsonCreator
    public JointProcurement(@JsonProperty("isJointProcurement") final Boolean isJointProcurement,
                            @JsonProperty("country") final String country) {
        this.isJointProcurement = isJointProcurement;
        this.country = country;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isJointProcurement)
                .append(country)
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
        return new EqualsBuilder().append(isJointProcurement, rhs.isJointProcurement)
                .append(country, rhs.country)
                .isEquals();
    }
}
