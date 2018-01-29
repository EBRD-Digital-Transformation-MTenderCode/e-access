
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hasDynamicPurchasingSystem",
    "hasOutsideBuyerAccess",
    "noFurtherContracts"
})
public class DynamicPurchasingSystem {
    @JsonProperty("hasDynamicPurchasingSystem")
    @JsonPropertyDescription("A True/False field to indicate whether a Dynamic Purchasing System has been set up.")
    private final Boolean hasDynamicPurchasingSystem;

    @JsonProperty("hasOutsideBuyerAccess")
    @JsonPropertyDescription("A True/False field to indicate whether the Dynamic Purchasing System may be used by " +
        "buyers outside the notice.")
    private final Boolean hasOutsideBuyerAccess;

    @JsonProperty("noFurtherContracts")
    @JsonPropertyDescription("A True/False field to indicate whether no further contracts will be awarded in this " +
        "dynamic purchasing system.")
    private final Boolean noFurtherContracts;

    @JsonCreator
    public DynamicPurchasingSystem(@JsonProperty("hasDynamicPurchasingSystem") final Boolean hasDynamicPurchasingSystem,
                                   @JsonProperty("hasOutsideBuyerAccess") final Boolean hasOutsideBuyerAccess,
                                   @JsonProperty("noFurtherContracts") final Boolean noFurtherContracts) {
        this.hasDynamicPurchasingSystem = hasDynamicPurchasingSystem;
        this.hasOutsideBuyerAccess = hasOutsideBuyerAccess;
        this.noFurtherContracts = noFurtherContracts;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasDynamicPurchasingSystem)
                                    .append(hasOutsideBuyerAccess)
                                    .append(noFurtherContracts)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof DynamicPurchasingSystem)) {
            return false;
        }
        final DynamicPurchasingSystem rhs = (DynamicPurchasingSystem) other;
        return new EqualsBuilder().append(hasDynamicPurchasingSystem, rhs.hasDynamicPurchasingSystem)
                                  .append(hasOutsideBuyerAccess, rhs.hasOutsideBuyerAccess)
                                  .append(noFurtherContracts, rhs.noFurtherContracts)
                                  .isEquals();
    }
}
