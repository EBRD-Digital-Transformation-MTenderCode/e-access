package com.procurement.access.model.dto.ocds;


import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "address",
        "description",
        "NUTScode"
})
public class PlaceOfPerformance {

    @JsonProperty("address")
    @JsonPropertyDescription("The address, NUTS code and further description of the place where the contract will be " +
            "performed")
    private final Address address;

    @JsonProperty("description")
    @JsonPropertyDescription("Further description of the place of performance of the contract. Required by EU.")
    private final String description;

    @JsonProperty("NUTScode")
    @JsonPropertyDescription("NUTS code for the place of performance of the contract.")
    private final String NUTScode;

    @JsonCreator
    public PlaceOfPerformance(@JsonProperty("address") final Address address,
                              @JsonProperty("description") final String description,
                              @JsonProperty("NUTScode") final String NUTScode) {
        this.address = address;
        this.description = description;
        this.NUTScode = NUTScode;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(address)
                .append(description)
                .append(NUTScode)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PlaceOfPerformance)) {
            return false;
        }
        final PlaceOfPerformance rhs = (PlaceOfPerformance) other;
        return new EqualsBuilder()
                .append(address, rhs.address)
                .append(description, rhs.description)
                .append(NUTScode, rhs.NUTScode)
                .isEquals();
    }
}
