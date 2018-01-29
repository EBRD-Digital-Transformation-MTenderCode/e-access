package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "hasVariants",
        "variantDetails"
})
public class Variant {
    @JsonProperty("hasVariants")
    private final Boolean hasVariants;

    @JsonProperty("variantDetails")
    private final String variantDetails;

    @JsonCreator
    public Variant(@JsonProperty("hasVariants") final Boolean hasVariants,
                   @JsonProperty("variantDetails") final String variantDetails) {
        super();
        this.hasVariants = hasVariants;
        this.variantDetails = variantDetails;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(hasVariants)
                .append(variantDetails)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Variant)) {
            return false;
        }
        final Variant rhs = ((Variant) other);
        return new EqualsBuilder().append(hasVariants, rhs.hasVariants)
                .append(variantDetails, rhs.variantDetails)
                .isEquals();
    }
}
