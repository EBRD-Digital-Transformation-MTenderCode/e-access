package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "streetAddress",
        "locality",
        "region",
        "postalCode",
        "countryName"
})
public class CnAddressDto {
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
    private final String countryName;

    @JsonCreator
    public CnAddressDto(@JsonProperty("streetAddress") final String streetAddress,
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
}
