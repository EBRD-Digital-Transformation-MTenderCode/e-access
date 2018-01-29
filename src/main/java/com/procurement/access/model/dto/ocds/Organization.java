package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import java.net.URI;
import java.util.*;
import javax.validation.Valid;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "name",
        "id",
        "identifier",
        "additionalIdentifiers",
        "address",
        "contactPoint",
        "roles",
        "details",
        "buyerProfile"
})
public class Organization {
    @JsonProperty("id")
    private final String id;

    @JsonProperty("name")
    private final String name;

    @JsonProperty("identifier")
    @Valid
    private final Identifier identifier;

    @JsonProperty("additionalIdentifiers")
    @Valid
    private final Set<Identifier> additionalIdentifiers;

    @JsonProperty("address")
    @Valid
    private final Address address;

    @JsonProperty("contactPoint")
    @Valid
    private final ContactPoint contactPoint;

    @JsonProperty("roles")
    private final List<PartyRole> roles;

    @JsonProperty("details")
    private final Details details;

    @JsonProperty("buyerProfile")
    private final String buyerProfile;

    @JsonCreator
    public Organization(@JsonProperty("name") final String name,
                        @JsonProperty("id") final String id,
                        @JsonProperty("identifier") final Identifier identifier,
                        @JsonProperty("additionalIdentifiers") final LinkedHashSet<Identifier> additionalIdentifiers,
                        @JsonProperty("address") final Address address,
                        @JsonProperty("contactPoint") final ContactPoint contactPoint,
                        @JsonProperty("roles") final List<PartyRole> roles,
                        @JsonProperty("details") final Details details,
                        @JsonProperty("buyerProfile") final String buyerProfile) {
        this.name = name;
        this.id = id;
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
        return new HashCodeBuilder().append(name)
                .append(id)
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
        if (!(other instanceof Organization)) {
            return false;
        }
        final Organization rhs = (Organization) other;
        return new EqualsBuilder().append(name, rhs.name)
                .append(id, rhs.id)
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

        private final static Map<String, PartyRole> CONSTANTS = new HashMap<>();

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
