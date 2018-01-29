
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hasPrizes",
    "prizes",
    "paymentsToParticipants",
    "serviceContractAward",
    "juryDecisionBinding",
    "juryMembers",
    "participants"
})
public class DesignContest {
    @JsonProperty("hasPrizes")
    private final Boolean hasPrizes;

    @JsonProperty("prizes")
    @Valid
    private final Set<Item> prizes;

    @JsonProperty("paymentsToParticipants")
    private final String paymentsToParticipants;

    @JsonProperty("serviceContractAward")
    private final Boolean serviceContractAward;

    @JsonProperty("juryDecisionBinding")
    private final Boolean juryDecisionBinding;

    @JsonProperty("juryMembers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<OrganizationReference> juryMembers;

    @JsonProperty("participants")
    @JsonDeserialize(as = LinkedHashSet.class)
    @Valid
    private final Set<OrganizationReference> participants;

    @JsonCreator
    public DesignContest(@JsonProperty("hasPrizes") final Boolean hasPrizes,
                         @JsonProperty("prizes") final LinkedHashSet<Item> prizes,
                         @JsonProperty("paymentsToParticipants") final String paymentsToParticipants,
                         @JsonProperty("serviceContractAward") final Boolean serviceContractAward,
                         @JsonProperty("juryDecisionBinding") final Boolean juryDecisionBinding,
                         @JsonProperty("juryMembers") final LinkedHashSet<OrganizationReference> juryMembers,
                         @JsonProperty("participants") final LinkedHashSet<OrganizationReference> participants) {
        this.hasPrizes = hasPrizes;
        this.prizes = prizes;
        this.paymentsToParticipants = paymentsToParticipants;
        this.serviceContractAward = serviceContractAward;
        this.juryDecisionBinding = juryDecisionBinding;
        this.juryMembers = juryMembers;
        this.participants = participants;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasPrizes)
                                    .append(prizes)
                                    .append(paymentsToParticipants)
                                    .append(serviceContractAward)
                                    .append(juryDecisionBinding)
                                    .append(juryMembers)
                                    .append(participants)
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
        return new EqualsBuilder().append(hasPrizes, rhs.hasPrizes)
                                  .append(prizes, rhs.prizes)
                                  .append(paymentsToParticipants, rhs.paymentsToParticipants)
                                  .append(serviceContractAward, rhs.serviceContractAward)
                                  .append(juryDecisionBinding, rhs.juryDecisionBinding)
                                  .append(juryMembers, rhs.juryMembers)
                                  .append(participants, rhs.participants)
                                  .isEquals();
    }
}
