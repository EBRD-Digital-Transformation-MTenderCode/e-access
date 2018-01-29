
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "maximumLotsBidPerSupplier",
    "maximumLotsAwardedPerSupplier"
})
public class LotDetails {
    @JsonProperty("maximumLotsBidPerSupplier")
    @JsonPropertyDescription("The maximum number of lots that one supplier may bid for as part of this contracting " +
        "process.")
    private final Integer maximumLotsBidPerSupplier;

    @JsonProperty("maximumLotsAwardedPerSupplier")
    @JsonPropertyDescription("The maximum number of lots that may be awarded to one supplier as part of this " +
        "contracting process.")
    private final Integer maximumLotsAwardedPerSupplier;

    @JsonCreator
    public LotDetails(@JsonProperty("maximumLotsBidPerSupplier") final Integer maximumLotsBidPerSupplier,
                      @JsonProperty("maximumLotsAwardedPerSupplier") final Integer maximumLotsAwardedPerSupplier) {
        this.maximumLotsBidPerSupplier = maximumLotsBidPerSupplier;
        this.maximumLotsAwardedPerSupplier = maximumLotsAwardedPerSupplier;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maximumLotsBidPerSupplier)
                                    .append(maximumLotsAwardedPerSupplier)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LotDetails)) {
            return false;
        }
        final LotDetails rhs = (LotDetails) other;
        return new EqualsBuilder().append(maximumLotsBidPerSupplier, rhs.maximumLotsBidPerSupplier)
                                  .append(maximumLotsAwardedPerSupplier, rhs.maximumLotsAwardedPerSupplier)
                                  .isEquals();
    }
}
