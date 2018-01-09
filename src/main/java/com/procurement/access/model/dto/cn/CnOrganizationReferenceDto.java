package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "id",
        "identifier",
        "address",
        "additionalIdentifiers",
        "contactPoint"
})
public class CnOrganizationReferenceDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The id of the party being referenced. This must match the id of an entry in the parties" +
            " section.")
    @NotNull
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("The name of the party being referenced. This must match the name of an entry in the " +
            "parties section.")
    @Size(min = 1)
    @NotNull
    private final String name;

    @JsonProperty("identifier")
    @Valid
    private final CnIdentifierDto identifier;

    @JsonProperty("address")
    @Valid
    private final CnAddressDto address;

    @JsonProperty("additionalIdentifiers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("(Deprecated outside the parties section) A list of additional / supplemental " +
            "identifiers for the organization, using the [organization identifier guidance](http://standard" +
            ".open-contracting.org/latest/en/schema/identifiers/). This could be used to provide an internally used " +
            "identifier for this organization in addition to the primary legal entity identifier.")
    @Valid
    private final Set<CnIdentifierDto> additionalIdentifiers;

    @JsonProperty("contactPoint")
    @Valid
    private final CnContactPointDto contactPoint;

    @JsonCreator
    public CnOrganizationReferenceDto(@JsonProperty("name") final String name,
                                 @JsonProperty("id") final String id,
                                 @JsonProperty("identifier") final CnIdentifierDto identifier,
                                 @JsonProperty("address") final CnAddressDto address,
                                 @JsonProperty("additionalIdentifiers") final LinkedHashSet<CnIdentifierDto>
                                         additionalIdentifiers,
                                 @JsonProperty("contactPoint") final CnContactPointDto contactPoint) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.address = address;
        this.additionalIdentifiers = additionalIdentifiers;
        this.contactPoint = contactPoint;
    }

}
