package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class Renewal {

    @NotNull
    @JsonProperty("hasRenewals")
    private final Boolean hasRenewals;

    @JsonCreator
    public Renewal(@JsonProperty("hasRenewals") final Boolean hasRenewals) {
        this.hasRenewals = hasRenewals;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(hasRenewals)
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
        return new EqualsBuilder()
                .append(hasRenewals, rhs.hasRenewals)
                .isEquals();
    }
}
