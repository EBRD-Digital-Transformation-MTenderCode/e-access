
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
@JsonPropertyOrder({
        "serviceContractAward"
})
public class DesignContestDto {

    @JsonProperty("serviceContractAward")
    @JsonPropertyDescription("A True/False field to indicate whether a service contract will be awarded to the winner" +
        "(s) of the design contest. Required by the EU")
    @NotNull
    private final Boolean serviceContractAward;



    @JsonCreator
    public DesignContestDto(
                         @JsonProperty("serviceContractAward") final Boolean serviceContractAward) {

        this.serviceContractAward = serviceContractAward;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(serviceContractAward)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DesignContestDto)) {
            return false;
        }
        final DesignContestDto rhs = (DesignContestDto) other;
        return new EqualsBuilder().append(serviceContractAward, rhs.serviceContractAward)
                                  .isEquals();
    }
}
