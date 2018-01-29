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
        "hasDynamicPurchasingSystem",
        "hasOutsideBuyerAccess",
        "noFurtherContracts"
})
public class DynamicPurchasingSystem {
    @JsonProperty("hasDynamicPurchasingSystem")
    private final Boolean hasDynamicPurchasingSystem;

    @JsonProperty("hasOutsideBuyerAccess")
    private final Boolean hasOutsideBuyerAccess;

    @JsonProperty("noFurtherContracts")
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
