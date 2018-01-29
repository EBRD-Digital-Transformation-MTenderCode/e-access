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
        "streetAddress",
        "locality",
        "region",
        "postalCode",
        "countryName"
})
public class Address {
    @JsonProperty("streetAddress")
    private final String streetAddress;

    @JsonProperty("locality")
    private final String locality;

    @JsonProperty("region")
    private final String region;

    @JsonProperty("postalCode")
    private final String postalCode;

    @JsonProperty("countryName")
    private final String countryName;

    @JsonCreator
    public Address(@JsonProperty("streetAddress") final String streetAddress,
                   @JsonProperty("locality") final String locality,
                   @JsonProperty("region") final String region,
                   @JsonProperty("postalCode") final String postalCode,
                   @JsonProperty("countryName") final String countryName) {
        this.streetAddress = streetAddress;
        this.locality = locality;
        this.region = region;
        this.postalCode = postalCode;
        this.countryName = countryName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(streetAddress)
                .append(locality)
                .append(region)
                .append(postalCode)
                .append(countryName)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Address)) {
            return false;
        }
        final Address rhs = (Address) other;
        return new EqualsBuilder().append(streetAddress, rhs.streetAddress)
                .append(locality, rhs.locality)
                .append(region, rhs.region)
                .append(postalCode, rhs.postalCode)
                .append(countryName, rhs.countryName)
                .isEquals();
    }
}
