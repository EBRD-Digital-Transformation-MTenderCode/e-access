
package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.procurement.access.model.dto.databinding.LocalDateTimeDeserializer;
import com.procurement.access.model.dto.databinding.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "ocid",
    "id",
    "date",
    "tag",
    "initiationType",
    "title",
    "description",
    "parties",
    "planning",
    "tender",
    "buyer",
    "awards",
    "contracts",
    "language",
    "relatedProcesses",
    "bids",
    "buyerInternalReferenceId",
    "hasPreviousNotice",
    "purposeOfNotice",
    "relatedNotice"
})
public class ReleaseExt {
    @JsonProperty("ocid")
    @JsonPropertyDescription("A globally unique identifier for this Open Contracting Process. Composed of a publisher" +
        " prefix and an identifier for the contracting process. For more information see the [Open Contracting " +
        "Identifier guidance](http://standard.open-contracting.org/latest/en/schema/identifiers/)")
    @Size(min = 1)
    @NotNull
    private final String ocid;

    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this particular release of information. A release identifier must be " +
        "unique within the scope of its related contracting process (defined by a common ocid), and unique within any" +
        " release package it appears in. A release identifier must not contain the # character.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("date")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonPropertyDescription("The date this information was first released, or published.")
    @NotNull
    private final LocalDateTime date;

    @JsonProperty("tag")
    @JsonPropertyDescription("One or more values from the [releaseTag codelist](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#release-tag). Tags may be used to filter release and to understand the kind" +
        " of information that a release might contain.")
    @Size(min = 1)
    @Valid
    @NotNull
    private final List<Tag> tag;

    @JsonProperty("initiationType")
    @JsonPropertyDescription("String specifying the type of initiation process used for this contract, taken from the" +
        " [initiationType](http://standard.open-contracting.org/latest/en/schema/codelists/#initiation-type) codelist" +
        ". Currently only tender is supported.")
    @NotNull
    private final InitiationType initiationType;

    @JsonProperty("title")
    @JsonPropertyDescription("A overall title for this contracting process or release.")
    private final String title;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("parties")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("Information on the parties (organizations, economic operators and other participants) " +
        "who are involved in the contracting process and their roles, e.g. buyer, procuring entity, supplier etc. " +
        "Organization references elsewhere in the schema are used to refer back to this entries in this list.")
    @Valid
    private final Set<Organization> parties;

    @JsonProperty("buyer")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
        "section")
    @Valid
    private final OrganizationReference buyer;

    @JsonProperty("planning")
    @JsonPropertyDescription("Information from the planning phase of the contracting process. Note that many other " +
        "fields may be filled in a planning release, in the appropriate fields in other schema sections, these would " +
        "likely be estimates at this stage e.g. totalValue in tender")
    @Valid
    private final Planning planning;

    @JsonProperty("tender")
    @JsonPropertyDescription("Data regarding tender process - publicly inviting prospective contractors to submit " +
        "bids for evaluation and selecting a winner or winners.")
    @Valid
    private final Tender tender;

    @JsonProperty("awards")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("Information from the award phase of the contracting process. There may be more than one" +
        " award per contracting process e.g. because the contract is split among different providers, or because it " +
        "is a standing offer.")
    @Valid
    private final Set<Award> awards;

    @JsonProperty("contracts")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("Information from the contract creation phase of the procurement process.")
    @Valid
    private final Set<Contract> contracts;

    @JsonProperty("language")
    @JsonPropertyDescription("Specifies the default language of the data using either two-letter [ISO639-1]" +
        "(https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes), or extended [BCP47 language tags](http://www" +
        ".w3.org/International/articles/language-tags/). The use of lowercase two-letter codes from [ISO639-1]" +
        "(https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) is strongly recommended.")
    private final String language;

    @JsonProperty("relatedProcesses")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("If this process follows on from one or more prior process, represented under a separate" +
        " open contracting identifier (ocid) then details of the related process can be provided here. This is " +
        "commonly used to relate mini-competitions to their parent frameworks, full tenders to a pre-qualification " +
        "phase, or individual tenders to a broad planning process.")
    @Valid
    private final Set<RelatedProcess> relatedProcesses;

    @JsonProperty("bids")
    @JsonPropertyDescription("The bid section is used to publish summary statistics, and where applicable, individual" +
        " bid information.")
    private final Bids bids;

    @JsonProperty("buyerInternalReferenceId")
    @JsonPropertyDescription("The buyer internal reference identifier is an EU specific field. It uniquely identifies" +
        " a procurement process within the Buyer's internal system.")
    private final String buyerInternalReferenceId;

    @JsonProperty("hasPreviousNotice")
    @JsonPropertyDescription("A True or False field to indicate whether this release represents a TED notice that is " +
        "connected to a previous notice, either TED or national. Required by EU.")
    private final Boolean hasPreviousNotice;

    @JsonProperty("purposeOfNotice")
    @JsonPropertyDescription("Details about the purpose of this notice release - used to determine the fields in the " +
        "notice that are required to be completed. Required by EU.")
    private final PurposeOfNotice purposeOfNotice;

    @JsonProperty("relatedNotice")
    @JsonPropertyDescription("Information that connects a notice with a related notice for the contracting process.")
    private final List<RelatedNotice> relatedNotice;

    public ReleaseExt(@JsonProperty("ocid") final String ocid,
                      @JsonProperty("id") final String id,
                      @JsonProperty("date") @JsonDeserialize(using = LocalDateTimeDeserializer.class) final LocalDateTime date,
                      @JsonProperty("tag") final List<Tag> tag,
                      @JsonProperty("initiationType") final InitiationType initiationType,
                      @JsonProperty("title") final String title,
                      @JsonProperty("description") final String description,
                      @JsonProperty("parties") final LinkedHashSet<Organization> parties,
                      @JsonProperty("planning") final Planning planning,
                      @JsonProperty("tender") final Tender tender,
                      @JsonProperty("buyer") final OrganizationReference buyer,
                      @JsonProperty("awards") final LinkedHashSet<Award> awards,
                      @JsonProperty("contracts") final LinkedHashSet<Contract> contracts,
                      @JsonProperty("language") final String language,
                      @JsonProperty("relatedProcesses") final LinkedHashSet<RelatedProcess> relatedProcesses,
                      @JsonProperty("bids") final Bids bids,
                      @JsonProperty("buyerInternalReferenceId") final String buyerInternalReferenceId,
                      @JsonProperty("hasPreviousNotice") final Boolean hasPreviousNotice,
                      @JsonProperty("purposeOfNotice") final PurposeOfNotice purposeOfNotice,
                      @JsonProperty("relatedNotice") final List<RelatedNotice> relatedNotice) {
        this.ocid = ocid;
        this.id = id;
        this.date = date;
        this.tag = tag;
        this.initiationType = initiationType;
        this.title = title;
        this.description = description;
        this.parties = parties;
        this.planning = planning;
        this.tender = tender;
        this.buyer = buyer;
        this.awards = awards;
        this.contracts = contracts;
        this.language = language == null ? "en" : language;
        this.relatedProcesses = relatedProcesses;
        this.bids = bids;
        this.buyerInternalReferenceId = buyerInternalReferenceId;
        this.hasPreviousNotice = hasPreviousNotice;
        this.purposeOfNotice = purposeOfNotice;
        this.relatedNotice = relatedNotice;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ocid)
                                    .append(id)
                                    .append(date)
                                    .append(tag)
                                    .append(initiationType)
                                    .append(parties)
                                    .append(planning)
                                    .append(tender)
                                    .append(buyer)
                                    .append(awards)
                                    .append(contracts)
                                    .append(language)
                                    .append(relatedProcesses)
                                    .append(bids)
                                    .append(buyerInternalReferenceId)
                                    .append(hasPreviousNotice)
                                    .append(purposeOfNotice)
                                    .append(relatedNotice)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ReleaseExt)) {
            return false;
        }
        final ReleaseExt rhs = (ReleaseExt) other;
        return new EqualsBuilder().append(ocid, rhs.ocid)
                                  .append(id, rhs.id)
                                  .append(date, rhs.date)
                                  .append(tag, rhs.tag)
                                  .append(initiationType, rhs.initiationType)
                                  .append(parties, rhs.parties)
                                  .append(planning, rhs.planning)
                                  .append(tender, rhs.tender)
                                  .append(buyer, rhs.buyer)
                                  .append(awards, rhs.awards)
                                  .append(contracts, rhs.contracts)
                                  .append(language, rhs.language)
                                  .append(relatedProcesses, rhs.relatedProcesses)
                                  .append(bids, rhs.bids)
                                  .append(buyerInternalReferenceId, rhs.buyerInternalReferenceId)
                                  .append(hasPreviousNotice, rhs.hasPreviousNotice)
                                  .append(purposeOfNotice, rhs.purposeOfNotice)
                                  .append(relatedNotice, rhs.relatedNotice)
                                  .isEquals();
    }

    public enum InitiationType {
        TENDER("tender");

        private final String value;
        private final static Map<String, InitiationType> CONSTANTS = new HashMap<>();

        static {
            for (final InitiationType c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private InitiationType(final String value) {
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
        public static InitiationType fromValue(final String value) {
            final InitiationType constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(value);
            }
            return constant;
        }
    }
}
