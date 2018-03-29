package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
public class DynamicPurchasingSystem {

    @NotNull
    @JsonProperty("hasDynamicPurchasingSystem")
    private final Boolean hasDynamicPurchasingSystem;

    @JsonCreator
    public DynamicPurchasingSystem(@JsonProperty("hasDynamicPurchasingSystem") final Boolean hasDynamicPurchasingSystem) {
        this.hasDynamicPurchasingSystem = hasDynamicPurchasingSystem;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(hasDynamicPurchasingSystem)
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
        return new EqualsBuilder()
                .append(hasDynamicPurchasingSystem, rhs.hasDynamicPurchasingSystem)
                .isEquals();
    }
}
