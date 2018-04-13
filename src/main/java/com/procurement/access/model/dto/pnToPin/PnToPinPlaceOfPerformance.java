package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Address;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "address",
        "description"
})
public class PnToPinPlaceOfPerformance {

    @NotNull
    @Valid
    @JsonProperty("address")
    private final Address address;

    @JsonProperty("description")
    private final String description;

    @JsonCreator
    public PnToPinPlaceOfPerformance(@JsonProperty("address") final Address address,
                                     @JsonProperty("description") final String description) {
        this.address = address;
        this.description = description;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(address)
                .append(description)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnToPinPlaceOfPerformance)) {
            return false;
        }
        final PnToPinPlaceOfPerformance rhs = (PnToPinPlaceOfPerformance) other;
        return new EqualsBuilder()
                .append(address, rhs.address)
                .append(description, rhs.description)
                .isEquals();
    }
}
