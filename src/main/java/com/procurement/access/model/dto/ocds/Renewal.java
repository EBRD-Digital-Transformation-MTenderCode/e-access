
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
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
    @JsonPropertyDescription("A True/False field to indicate whether contract renewals are allowed.")
    private final Boolean hasRenewals;

    @JsonProperty("maxNumber")
    @JsonPropertyDescription("Maximum number of renewals of this lot")
    private final Integer maxNumber;

    @JsonProperty("renewalConditions")
    @JsonPropertyDescription("Conditions for, and descriptions of, any renewals of this lot")
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
