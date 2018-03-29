package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class DesignContest {

    @NotNull
    @JsonProperty("serviceContractAward")
    private final Boolean serviceContractAward;

    @JsonCreator
    public DesignContest(@JsonProperty("serviceContractAward") final Boolean serviceContractAward) {

        this.serviceContractAward = serviceContractAward;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(serviceContractAward)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DesignContest)) {
            return false;
        }
        final DesignContest rhs = (DesignContest) other;
        return new EqualsBuilder()
                .append(serviceContractAward, rhs.serviceContractAward)
                .isEquals();
    }
}
