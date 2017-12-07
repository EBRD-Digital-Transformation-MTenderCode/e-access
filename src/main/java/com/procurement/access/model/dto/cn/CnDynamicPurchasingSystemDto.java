
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
@JsonPropertyOrder("hasDynamicPurchasingSystem")
public class CnDynamicPurchasingSystemDto {
    @JsonProperty("hasDynamicPurchasingSystem")
    @JsonPropertyDescription("A True/False field to indicate whether a Dynamic Purchasing System has been set up.")
    @NotNull
    private final Boolean hasDynamicPurchasingSystem;

    @JsonCreator
    public CnDynamicPurchasingSystemDto(@JsonProperty("hasDynamicPurchasingSystem")
                                            final Boolean hasDynamicPurchasingSystem) {
        this.hasDynamicPurchasingSystem = hasDynamicPurchasingSystem;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasDynamicPurchasingSystem)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof CnDynamicPurchasingSystemDto)) {
            return false;
        }
        final CnDynamicPurchasingSystemDto rhs = (CnDynamicPurchasingSystemDto) other;
        return new EqualsBuilder().append(hasDynamicPurchasingSystem, rhs.hasDynamicPurchasingSystem)
                                  .isEquals();
    }
}
