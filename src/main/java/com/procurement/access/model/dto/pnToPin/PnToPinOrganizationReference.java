package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.Address;
import com.procurement.access.model.dto.ocds.ContactPoint;
import com.procurement.access.model.dto.ocds.Identifier;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "identifier",
        "additionalIdentifiers",
        "address",
        "contactPoint"
})
public class PnToPinOrganizationReference {

    @NotNull
    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("name")
    private final String name;

    @Valid
    @JsonProperty("identifier")
    private final Identifier identifier;

    @Valid
    @JsonProperty("additionalIdentifiers")
    private final Set<Identifier> additionalIdentifiers;

    @Valid
    @JsonProperty("address")
    private final Address address;

    @Valid
    @NotNull
    @JsonProperty("contactPoint")
    private final ContactPoint contactPoint;

    @JsonCreator
    public PnToPinOrganizationReference(@JsonProperty("name") final String name,
                                        @JsonProperty("id") final String id,
                                        @JsonProperty("identifier") final Identifier identifier,
                                        @JsonProperty("address") final Address address,
                                        @JsonProperty("additionalIdentifiers") final HashSet<Identifier>
                                         additionalIdentifiers,
                                        @JsonProperty("contactPoint") final ContactPoint contactPoint) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.address = address;
        this.additionalIdentifiers = additionalIdentifiers;
        this.contactPoint = contactPoint;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                .append(id)
                .append(identifier)
                .append(address)
                .append(additionalIdentifiers)
                .append(contactPoint)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PnToPinOrganizationReference)) {
            return false;
        }
        final PnToPinOrganizationReference rhs = (PnToPinOrganizationReference) other;
        return new EqualsBuilder().append(name, rhs.name)
                .append(id, rhs.id)
                .append(identifier, rhs.identifier)
                .append(address, rhs.address)
                .append(additionalIdentifiers, rhs.additionalIdentifiers)
                .append(contactPoint, rhs.contactPoint)
                .isEquals();
    }
}
