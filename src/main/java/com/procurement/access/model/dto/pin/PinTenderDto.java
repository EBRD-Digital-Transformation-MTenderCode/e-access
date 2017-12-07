package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.procurement.access.model.dto.enums.ExtendedProcurementCategoryTender;
import com.procurement.access.model.dto.enums.MainProcurementCategoryTender;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@JsonPropertyOrder({
    "id",
    "title",
    "description",
    "status",
    "items",
    "value",
    "procurementMethod",
    "procurementMethodDetails",
    "mainProcurementCategory",
    "additionalProcurementCategories",
    "awardCriteria",
    "submissionMethod",
    "submissionMethodDetails",
    "contractPeriod",
    "lots",
    "lotGroups",
    "classification",
    "electronicWorkflows",
    "jointProcurement",
    "legalBasis",
    "dynamicPurchasingSystem",
    "framework"
})
public class PinTenderDto {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this tender process. This may be the same as the ocid, or may be " +
        "drawn from an internally held identifier for this tender.")
    @Size(min = 1)
    @NotNull
    private final String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this tender. This will often be used by applications as a headline to " +
        "attract interest, and to help analysts understand the nature of this procurement.")
    @NotNull
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the tender. This should complement structured information " +
        "provided using the items array. Descriptions should be short and easy to read. Avoid using ALL CAPS. ")
    @NotNull
    private final String description;

    @JsonProperty("status")
    @JsonPropertyDescription("The current status of the tender based on the [tenderStatus codelist](http://standard" +
        ".open-contracting.org/latest/en/schema/codelists/#tender-status)")
    @NotNull
    private final PinTenderStatusDto status;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The goods and services to be purchased, broken into line items wherever possible. Items" +
        " should not be duplicated, but a quantity of 2 specified instead.")
    @Valid
    @NotNull
    private final Set<PinItemDto> items;

    @JsonProperty("value")
    @Valid
    @NotNull
    private final PinValueDto value;

    @JsonProperty("procurementMethod")
    @JsonPropertyDescription("Specify tendering method using the [method codelist](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#method). This is a closed codelist. Local method types should be mapped to " +
        "this list.")
    @Valid
    @NotNull
    private final ProcurementMethod procurementMethod;

    @JsonProperty("procurementMethodDetails")
    @JsonPropertyDescription("Additional detail on the procurement method used. This field may be used to provide the" +
        " local name of the particular procurement method used.")
    @NotNull
    private final String procurementMethodDetails;

    @JsonProperty("mainProcurementCategory")
    @JsonPropertyDescription("The primary category describing the main object of this contracting process from the " +
        "[procurementCategory](http://standard.open-contracting.org/latest/en/schema/codelists/#procurement-category)" +
        " codelist. This is a closed codelist. Local classifications should be mapped to this list.")
    @Valid
    @NotNull
    private final MainProcurementCategoryTender mainProcurementCategory;

    @JsonProperty("additionalProcurementCategories")
    @JsonPropertyDescription("Any additional categories which describe the objects of this contracting process, from " +
        "the [extendedProcurementCategory](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#extended-procurement-category) codelist. This is an open codelist. Local " +
        "categories can be included in this list.")
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final List<ExtendedProcurementCategoryTender> additionalProcurementCategories;

    @JsonProperty("awardCriteria")
    @JsonPropertyDescription("Specify the award criteria for the procurement, using the [award criteria codelist]" +
        "(http://standard.open-contracting.org/latest/en/schema/codelists/#award-criteria)")
    @NotNull
    private final AwardCriteria awardCriteria;

    @JsonProperty("submissionMethod")
    @JsonPropertyDescription("Specify the method by which bids must be submitted, in person, written, or electronic " +
        "auction. Using the [submission method codelist](http://standard.open-contracting" +
        ".org/latest/en/schema/codelists/#submission-method)")
    @NotNull
    private final List<SubmissionMethod> submissionMethod;

    @JsonProperty("submissionMethodDetails")
    @JsonPropertyDescription("Any detailed or further information on the submission method. This may include the " +
        "address, e-mail address or online service to which bids should be submitted, and any special requirements to" +
        " be followed for submissions.")
    @NotNull
    private final String submissionMethodDetails;

    @JsonProperty("contractPeriod")
    @Valid
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private final PinTenderContracPeriodDto contractPeriod;

    @JsonProperty("lots")
    @JsonPropertyDescription("A tender process may be divided into lots, where bidders can bid on one or more lots. " +
        "Details of each lot can be provided here. Items, documents and other features can then reference the lot " +
        "they are related to using relatedLot. Where no relatedLot identifier is given, the values should be " +
        "interpreted as applicable to the whole tender. Properties of tender can be overridden for a given CnLotDto " +
        "through their inclusion in the CnLotDto object.")
    @Valid
    @NotNull
    private final List<PinLotDto> lots;

    @JsonProperty("lotGroups")
    @JsonPropertyDescription("ere the buyer reserves the right to combine lots, or wishes to specify the total value " +
        "for a group of lots, a lot group is used to capture this information.")
    @Valid
    @NotNull
    private final List<PinLotGroupDto> lotGroups;

    @JsonProperty("classification")
    @Valid
    @NotNull
    private final PinClassificationDto classification;

    @JsonProperty("electronicWorkflows")
    @Valid
    @NotNull
    private final PinElectronicWorkflowsDto electronicWorkflows;

    @JsonProperty("jointProcurement")
    @Valid
    @NotNull
    private final PinJointProcurementDto jointProcurement;

    @JsonProperty("legalBasis")
    @JsonPropertyDescription("The legal basis of the tender based on the [legalBasis codelist](http://standard" +
        ".open-contracting.org/......")
    @NotNull
    private final LegalBasis legalBasis;

    @JsonProperty("dynamicPurchasingSystem")
    @JsonPropertyDescription("Dynamic Purchasing System: Whether a dynamic purchasing system has been set up and if " +
        "so whether it may be used by buyers outside the notice. Required by EU.")
    @Valid
    @NotNull
    private final PinDynamicPurchasingSystemDto dynamicPurchasingSystem;

    @JsonProperty("framework")
    @JsonPropertyDescription("The details of any framework agreement established as part of this procurement. " +
        "Required by EU.")
    @Valid
    @NotNull
    private final PinFrameworkDto framework;

    @JsonCreator
    public PinTenderDto(@JsonProperty("id") final String id,
                        @JsonProperty("title") final String title,
                        @JsonProperty("description") final String description,
                        @JsonProperty("status") final PinTenderStatusDto status,
                        @JsonProperty("items") final LinkedHashSet<PinItemDto> items,
                        @JsonProperty("value") final PinValueDto value,
                        @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                        @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                        @JsonProperty("mainProcurementCategory") final MainProcurementCategoryTender
                                mainProcurementCategory,
                        @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategoryTender>
                            additionalProcurementCategories,
                        @JsonProperty("awardCriteria") final AwardCriteria awardCriteria,
                        @JsonProperty("submissionMethod") final List<SubmissionMethod> submissionMethod,
                        @JsonProperty("submissionMethodDetails") final String submissionMethodDetails,
                        @JsonProperty("contractPeriod") final PinTenderContracPeriodDto contractPeriod,
                        @JsonProperty("lots") final List<PinLotDto> lots,
                        @JsonProperty("lotGroups") final List<PinLotGroupDto> lotGroups,
                        @JsonProperty("classification") final PinClassificationDto classification,
                        @JsonProperty("electronicWorkflows") final PinElectronicWorkflowsDto electronicWorkflows,
                        @JsonProperty("jointProcurement") final PinJointProcurementDto jointProcurement,
                        @JsonProperty("legalBasis") final LegalBasis legalBasis,
                        @JsonProperty("dynamicPurchasingSystem") final PinDynamicPurchasingSystemDto
                            dynamicPurchasingSystem,
                        @JsonProperty("framework") final PinFrameworkDto framework) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.items = items;
        this.value = value;
        this.procurementMethod = procurementMethod;
        this.procurementMethodDetails = procurementMethodDetails;
        this.mainProcurementCategory = mainProcurementCategory;
        this.additionalProcurementCategories = additionalProcurementCategories;
        this.awardCriteria = awardCriteria;
        this.submissionMethod = submissionMethod;
        this.submissionMethodDetails = submissionMethodDetails;
        this.contractPeriod = contractPeriod;
        this.lots = lots;
        this.lotGroups = lotGroups;
        this.classification = classification;
        this.electronicWorkflows = electronicWorkflows;
        this.jointProcurement = jointProcurement;
        this.legalBasis = legalBasis;
        this.dynamicPurchasingSystem = dynamicPurchasingSystem;
        this.framework = framework;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                                    .append(title)
                                    .append(description)
                                    .append(status)
                                    .append(items)
                                    .append(value)
                                    .append(procurementMethod)
                                    .append(procurementMethodDetails)
                                    .append(mainProcurementCategory)
                                    .append(additionalProcurementCategories)
                                    .append(awardCriteria)
                                    .append(submissionMethod)
                                    .append(submissionMethodDetails)
                                    .append(contractPeriod)
                                    .append(lots)
                                    .append(lotGroups)
                                    .append(classification)
                                    .append(electronicWorkflows)
                                    .append(jointProcurement)
                                    .append(legalBasis)
                                    .append(dynamicPurchasingSystem)
                                    .append(framework)
                                    .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PinTenderDto)) {
            return false;
        }
        final PinTenderDto rhs = (PinTenderDto) other;
        return new EqualsBuilder().append(id, rhs.id)
                                  .append(title, rhs.title)
                                  .append(description, rhs.description)
                                  .append(status, rhs.status)
                                  .append(items, rhs.items)
                                  .append(value, rhs.value)
                                  .append(procurementMethod, rhs.procurementMethod)
                                  .append(procurementMethodDetails, rhs.procurementMethodDetails)
                                  .append(mainProcurementCategory, rhs.mainProcurementCategory)
                                  .append(additionalProcurementCategories, rhs.additionalProcurementCategories)
                                  .append(awardCriteria, rhs.awardCriteria)
                                  .append(submissionMethod, rhs.submissionMethod)
                                  .append(submissionMethodDetails, rhs.submissionMethodDetails)
                                  .append(contractPeriod, rhs.contractPeriod)
                                  .append(lots, rhs.lots)
                                  .append(lotGroups, rhs.lotGroups)
                                  .append(classification, rhs.classification)
                                  .append(electronicWorkflows, rhs.electronicWorkflows)
                                  .append(jointProcurement, rhs.jointProcurement)
                                  .append(legalBasis, rhs.legalBasis)
                                  .append(dynamicPurchasingSystem, rhs.dynamicPurchasingSystem)
                                  .append(framework, rhs.framework)
                                  .isEquals();
    }

    public enum ProcurementMethod {
        OPEN("open"),
        SELECTIVE("selective"),
        LIMITED("limited"),
        DIRECT("direct");

        private static final Map<String, ProcurementMethod> CONSTANTS = new HashMap<>();

        static {
            for (final ProcurementMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        ProcurementMethod(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ProcurementMethod fromValue(final String value) {
            final ProcurementMethod constant = CONSTANTS.get(value);
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

    public enum AwardCriteria {
        PRICE_ONLY("priceOnly"),
        COST_ONLY("costOnly"),
        QUALITY_ONLY("qualityOnly"),
        RATED_CRITERIA("ratedCriteria"),
        LOWEST_COST("lowestCost"),
        BEST_PROPOSAL("bestProposal"),
        BEST_VALUE_TO_GOVERNMENT("bestValueToGovernment"),
        SINGLE_BID_ONLY("singleBidOnly");

        private static final Map<String, AwardCriteria> CONSTANTS = new HashMap<>();

        static {
            for (final AwardCriteria c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        AwardCriteria(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static AwardCriteria fromValue(final String value) {
            final AwardCriteria constant = CONSTANTS.get(value);
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

    public enum SubmissionMethod {
        ELECTRONIC_SUBMISSION("electronicSubmission"),
        ELECTRONIC_AUCTION("electronicAuction"),
        WRITTEN("written"),
        IN_PERSON("inPerson");

        private static final Map<String, SubmissionMethod> CONSTANTS = new HashMap<>();

        static {
            for (final SubmissionMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        SubmissionMethod(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionMethod fromValue(final String value) {
            final SubmissionMethod constant = CONSTANTS.get(value);
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

    public enum LegalBasis {
        DIRECTIVE_2014_23_EU("DIRECTIVE_2014_23_EU"),
        DIRECTIVE_2014_24_EU("DIRECTIVE_2014_24_EU"),
        DIRECTIVE_2014_25_EU("DIRECTIVE_2014_25_EU"),
        DIRECTIVE_2009_81_EC("DIRECTIVE_2009_81_EC"),
        REGULATION_966_2012("REGULATION_966_2012"),
        NATIONAL_PROCUREMENT_LAW("NATIONAL_PROCUREMENT_LAW"),
        NULL("NULL");

        private static final Map<String, LegalBasis> CONSTANTS = new HashMap<>();

        static {
            for (final LegalBasis c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        LegalBasis(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static LegalBasis fromValue(final String value) {
            final LegalBasis constant = CONSTANTS.get(value);
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
