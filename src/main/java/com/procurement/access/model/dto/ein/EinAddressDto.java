package com.procurement.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.validation.constraints.Pattern;
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
public class EinAddressDto {
    @JsonProperty("streetAddress")
    @JsonPropertyDescription("The street address. For example, 1600 Amphitheatre Pkwy.")
    private final String streetAddress;

    @JsonProperty("locality")
    @JsonPropertyDescription("The locality. For example, Mountain View.")
    private final String locality;

    @JsonProperty("region")
    @JsonPropertyDescription("The region. For example, CA.")
    private final String region;

    @JsonProperty("postalCode")
    @JsonPropertyDescription("The postal code. For example, 94043.")
    private final String postalCode;

    @JsonProperty("countryName")
    @JsonPropertyDescription("The country name. For example, United States.")
//    @Pattern(regexp = "^(countryName_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5," +
//        "8})(-([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
//        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String countryName;

    @JsonCreator
    public EinAddressDto(@JsonProperty("streetAddress") final String streetAddress,
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
        if (!(other instanceof EinAddressDto)) {
            return false;
        }
        final EinAddressDto rhs = (EinAddressDto) other;
        return new EqualsBuilder().append(streetAddress, rhs.streetAddress)
                                  .append(locality, rhs.locality)
                                  .append(region, rhs.region)
                                  .append(postalCode, rhs.postalCode)
                                  .append(countryName, rhs.countryName)
                                  .isEquals();
    }
}
