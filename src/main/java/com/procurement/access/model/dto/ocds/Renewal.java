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
        "hasRenewals",
        "maxNumber",
        "renewalConditions"
})
public class Renewal {
    @JsonProperty("hasRenewals")
    private final Boolean hasRenewals;

    @JsonProperty("maxNumber")
    private final Integer maxNumber;

    @JsonProperty("renewalConditions")
    private final String renewalConditions;

    @JsonCreator
    public Renewal(@JsonProperty("hasRenewals") final Boolean hasRenewals,
                   @JsonProperty("maxNumber") final Integer maxNumber,
                   @JsonProperty("renewalConditions") final String renewalConditions) {
        this.hasRenewals = hasRenewals;
        this.maxNumber = maxNumber;
        this.renewalConditions = renewalConditions;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasRenewals)
                .append(maxNumber)
                .append(renewalConditions)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Renewal)) {
            return false;
        }
        final Renewal rhs = (Renewal) other;
        return new EqualsBuilder().append(hasRenewals, rhs.hasRenewals)
                .append(maxNumber, rhs.maxNumber)
                .append(renewalConditions, rhs.renewalConditions)
                .isEquals();
    }
}
