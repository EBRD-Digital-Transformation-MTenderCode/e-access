package com.procurement.access.model.dto.fs;

import com.fasterxml.jackson.annotation.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "identifier",
        "additionalIdentifiers",
        "address",
        "contactPoint",
        "roles",
        "details",
        "buyerProfile"
})
public class FsOrganizationDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The ID used for cross-referencing to this party from other sections of the release. " +
            "This field may be built with the following structure {identifier.scheme}-{identifier.id}" +
            "(-{department-identifier}).")
    @NotNull
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("A common name for this organization or other participant in the contracting process. " +
            "The identifier object provides an space for the formal legal name, and so this may either repeat that " +
            "value," +
            " or could provide the common name by which this organization or entity is known. This field may also " +
            "include" +
            " details of the department or sub-unit involved in this contracting process.")
    @NotNull
    private final String name;

    @JsonProperty("identifier")
    @JsonPropertyDescription("The primary identifier for this organization or participant. Identifiers that uniquely " +
            "pick out a legal entity should be preferred. Consult the [organization identifier guidance]" +
            "(http://standard" +
            ".open-contracting.org/latest/en/schema/identifiers/) for the preferred scheme and identifier to use.")
    @Valid
    @NotNull
    private final FsIdentifierDto identifier;

    @JsonProperty("additionalIdentifiers")
    @JsonPropertyDescription("A list of additional / supplemental identifiers for the organization or participant, " +
            "using the [organization identifier guidance](http://standard.open-contracting" +
            ".org/latest/en/schema/identifiers/). This could be used to provide an internally used identifier for " +
            "this " +
            "organization in addition to the primary legal entity identifier.")
    @Valid
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final Set<FsIdentifierDto> additionalIdentifiers;

    @JsonProperty("address")
    @JsonPropertyDescription("A list of additional / supplemental identifiers for the organization or participant, " +
            "using the [organization identifier guidance](http://standard.open-contracting" +
            ".org/latest/en/schema/identifiers/). This could be used to provide an internally used identifier for " +
            "this " +
            "organization in addition to the primary legal entity identifier.")
    @Valid
    @NotNull
    private final FsAddressDto address;

    @JsonProperty("contactPoint")
    @JsonPropertyDescription("The party's role(s) in the contracting process. Role(s) should be taken from the " +
            "[partyRole codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#party-role). " +
            "Values " +
            "from the provided codelist should be used wherever possible, though extended values can be provided if " +
            "the " +
            "codelist does not have a relevant code.")
    @Valid
    @NotNull
    private final FsContactPointDto contactPoint;

    @JsonProperty("roles")
    @JsonPropertyDescription("The party's role(s) in the contracting process. Role(s) should be taken from the " +
            "[partyRole codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#party-role). " +
            "Values " +
            "from the provided codelist should be used wherever possible, though extended values can be provided if " +
            "the " +
            "codelist does not have a relevant code.")
    @NotNull
    @Valid
    private final PartyRole roles;

    @JsonProperty("details")
    @JsonPropertyDescription("Additional classification information about parties can be provided using partyDetail " +
            "extensions that define particular properties and classification schemes. ")
    @NotNull
    @Valid
    private final FsDetailsDto details;

    @JsonProperty("buyerProfile")
    @JsonPropertyDescription("For buyer organisations only: the url of the organization's buyer profile. Specified by" +
            " the EU")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final String buyerProfile;

    @JsonCreator
    public FsOrganizationDto(@JsonProperty("id") final String id,
                             @JsonProperty("name") final String name,
                             @JsonProperty("identifier") final FsIdentifierDto identifier,
                             @JsonProperty("additionalIdentifiers") final LinkedHashSet<FsIdentifierDto>
                                     additionalIdentifiers,
                             @JsonProperty("address") final FsAddressDto address,
                             @JsonProperty("contactPoint") final FsContactPointDto contactPoint,
                             @JsonProperty("roles") final PartyRole roles,
                             @JsonProperty("details") final FsDetailsDto details,
                             @JsonProperty("buyerProfile") final String buyerProfile) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.additionalIdentifiers = additionalIdentifiers;
        this.address = address;
        this.contactPoint = contactPoint;
        this.roles = roles;
        this.details = details;
        this.buyerProfile = buyerProfile;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(name)
                .append(identifier)
                .append(additionalIdentifiers)
                .append(address)
                .append(contactPoint)
                .append(roles)
                .append(details)
                .append(buyerProfile)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FsOrganizationDto)) {
            return false;
        }
        final FsOrganizationDto rhs = (FsOrganizationDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(name, rhs.name)
                .append(identifier, rhs.identifier)
                .append(additionalIdentifiers, rhs.additionalIdentifiers)
                .append(address, rhs.address)
                .append(contactPoint, rhs.contactPoint)
                .append(roles, rhs.roles)
                .append(details, rhs.details)
                .append(buyerProfile, rhs.buyerProfile)
                .isEquals();
    }

    public enum PartyRole {
        BUYER("buyer"),
        PROCURING_ENTITY("procuringEntity"),
        SUPPLIER("supplier"),
        TENDERER("tenderer"),
        FUNDER("funder"),
        ENQUIRER("enquirer"),
        PAYER("payer"),
        PAYEE("payee"),
        REVIEW_BODY("reviewBody");

        static final Map<String, PartyRole> CONSTANTS = new HashMap<>();

        static {
            for (final PartyRole c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        PartyRole(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static PartyRole fromValue(final String value) {
            final PartyRole constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }
    }
}
