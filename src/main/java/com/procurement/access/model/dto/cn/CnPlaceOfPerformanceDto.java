package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "address",
        "description"
})
public class CnPlaceOfPerformanceDto {

    @JsonProperty("address")
    @JsonPropertyDescription("The address, NUTS code and further description of the place where the contract will be " +
            "performed")
    private final CnAddressDto address;

    @JsonProperty("description")
    @JsonPropertyDescription("Further description of the place of performance of the contract. Required by EU.")
    private final String description;


    @JsonCreator
    public CnPlaceOfPerformanceDto(@JsonProperty("address") final CnAddressDto address,
                                   @JsonProperty("description") final String description) {
        this.address = address;
        this.description = description;
    }
}
