
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "hasVariants",
    "variantDetails"
})
public class Variant {
    @JsonProperty("hasVariants")
    @JsonPropertyDescription("A True/False field to indicate if lot variants will be accepted. Required by the EU")
    private final Boolean hasVariants;

    @JsonProperty("variantDetails")
    @JsonPropertyDescription("Further information about the lot variants that will be accepted. Required by the EU")
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
