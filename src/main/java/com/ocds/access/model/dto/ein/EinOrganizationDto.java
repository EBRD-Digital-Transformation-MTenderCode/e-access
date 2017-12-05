package com.ocds.access.model.dto.ein;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
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
public class EinOrganizationDto {
    @JsonProperty("id")
    @JsonPropertyDescription("The ID used for cross-referencing to this party from other sections of the release. " +
        "This field may be built with the following structure {identifier.scheme}-{identifier.id}" +
        "(-{department-identifier}).")
    private final String id;

    @JsonProperty("name")
    @JsonPropertyDescription("A common name for this organization or other participant in the contracting process. " +
        "The identifier object provides an space for the formal legal name, and so this may either repeat that value," +
        " or could provide the common name by which this organization or entity is known. This field may also include" +
        " details of the department or sub-unit involved in this contracting process.")
    @Pattern(regexp = "^(name_(((([A-Za-z]{2,3}(-([A-Za-z]{3}(-[A-Za-z]{3}){0,2}))?)|[A-Za-z]{4}|[A-Za-z]{5,8})(-" +
        "([A-Za-z]{4}))?(-([A-Za-z]{2}|[0-9]{3}))?(-([A-Za-z0-9]{5,8}|[0-9][A-Za-z0-9]{3}))*(-([0-9A-WY-Za-wy-z]" +
        "(-[A-Za-z0-9]{2,8})+))*(-(x(-[A-Za-z0-9]{1,8})+))?)|(x(-[A-Za-z0-9]{1,8})+)))$")
    private final String name;

    @JsonProperty("identifier")
    @JsonPropertyDescription("The primary identifier for this organization or participant. Identifiers that uniquely " +
        "pick out a legal entity should be preferred. Consult the [organization identifier guidance](http://standard" +
        ".open-contracting.org/latest/en/schema/identifiers/) for the preferred scheme and identifier to use.")
    @Valid
    private final EinIdentifierDto identifier;

    @JsonProperty("additionalIdentifiers")
    @JsonPropertyDescription("A list of additional / supplemental identifiers for the organization or participant, " +
        "using the [organization identifier guidance](http://standard.open-contracting" +
        ".org/latest/en/schema/identifiers/). This could be used to provide an internally used identifier for this " +
        "organization in addition to the primary legal entity identifier.")
    @Valid
    private final Set<EinIdentifierDto> additionalIdentifiers;

    @JsonProperty("address")
    @JsonPropertyDescription("A list of additional / supplemental identifiers for the organization or participant, " +
        "using the [organization identifier guidance](http://standard.open-contracting" +
        ".org/latest/en/schema/identifiers/). This could be used to provide an internally used identifier for this " +
        "organization in addition to the primary legal entity identifier.")
    @Valid
    private final EinAddressDto address;

    @JsonProperty("contactPoint")
    @JsonPropertyDescription("The party's role(s) in the contracting process. Role(s) should be taken from the " +
        "[partyRole codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#party-role). Values " +
        "from the provided codelist should be used wherever possible, though extended values can be provided if the " +
        "codelist does not have a relevant code.")
    @Valid
    private final EinContactPointDto contactPoint;

    @JsonProperty("roles")
    @JsonPropertyDescription("The party's role(s) in the contracting process. Role(s) should be taken from the " +
        "[partyRole codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#party-role). Values " +
        "from the provided codelist should be used wherever possible, though extended values can be provided if the " +
        "codelist does not have a relevant code.")
    private final List<PartyRole> roles;

    @JsonProperty("details")
    @JsonPropertyDescription("Additional classification information about parties can be provided using partyDetail " +
        "extensions that define particular properties and classification schemes. ")
    private final EinDetailsDto details;

    @JsonProperty("buyerProfile")
    @JsonPropertyDescription("For buyer organisations only: the url of the organization's buyer profile. Specified by" +
        " the EU")
    private final String buyerProfile;

    @JsonCreator
    public EinOrganizationDto(@JsonProperty("id") final String id,
                              @JsonProperty("name") final String name,
                              @JsonProperty("identifier") final EinIdentifierDto identifier,
                              @JsonProperty("additionalIdentifiers") final LinkedHashSet<EinIdentifierDto>
                                  additionalIdentifiers,
                              @JsonProperty("address") final EinAddressDto address,
                              @JsonProperty("contactPoint") final EinContactPointDto contactPoint,
                              @JsonProperty("roles") final List<PartyRole> roles,
                              @JsonProperty("details") final EinDetailsDto details,
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
        if (!(other instanceof EinOrganizationDto)) {
            return false;
        }
        final EinOrganizationDto rhs = (EinOrganizationDto) other;
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

        private final String value;
        private final static Map<String, PartyRole> CONSTANTS = new HashMap<>();

        static {
            for (final PartyRole c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private PartyRole(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @JsonCreator
        public static PartyRole fromValue(final String value) {
            final PartyRole constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}
