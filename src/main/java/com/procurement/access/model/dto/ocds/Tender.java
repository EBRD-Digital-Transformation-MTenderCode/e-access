package com.procurement.access.model.dto.ocds;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.*;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "statusDetails",
        "items",
        "minValue",
        "value",
        "procurementMethod",
        "procurementMethodDetails",
        "procurementMethodRationale",
        "mainProcurementCategory",
        "additionalProcurementCategories",
        "awardCriteria",
        "awardCriteriaDetails",
        "submissionMethod",
        "submissionMethodDetails",
        "tenderPeriod",
        "enquiryPeriod",
        "hasEnquiries",
        "enquiries",
        "eligibilityCriteria",
        "awardPeriod",
        "contractPeriod",
        "numberOfTenderers",
        "tenderers",
        "procuringEntity",
        "documents",
        "milestones",
        "amendments",
        "amendment",
        "lots",
        "lotDetails",
        "lotGroups",
        "participationFees",
        "criteria",
        "acceleratedProcedure",
        "classification",
        "designContest",
        "electronicWorkflows",
        "jointProcurement",
        "legalBasis",
        "objectives",
        "procedureOutsourcing",
        "procurementMethodAdditionalInfo",
        "reviewParties",
        "reviewPeriod",
        "standstillPeriod",
        "submissionLanguages",
        "submissionMethodRationale",
        "dynamicPurchasingSystem",
        "framework",
        "requiresElectronicCatalogue"
})
public class Tender {
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this tender process. This may be the same as the ocid, or may be " +
            "drawn from an internally held identifier for this tender.")
    private String id;

    @JsonProperty("title")
    @JsonPropertyDescription("A title for this tender. This will often be used by applications as a headline to " +
            "attract interest, and to help analysts understand the nature of this procurement.")
    private final String title;

    @JsonProperty("description")
    @JsonPropertyDescription("A summary description of the tender. This should complement structured information " +
            "provided using the items array. Descriptions should be short and easy to read. Avoid using ALL CAPS. ")
    private final String description;

    @JsonProperty("status")
    @JsonPropertyDescription("The current status of the tender based on the [tenderStatus codelist](http://standard" +
            ".open-contracting.org/latest/en/schema/codelists/#tender-status)")
    private TenderStatus status;

    @JsonProperty("statusDetails")
    @JsonPropertyDescription("Additional details of status.)")
    private TenderStatusDetails statusDetails;

    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The goods and services to be purchased, broken into line items wherever possible. Items" +
            " should not be duplicated, but a quantity of 2 specified instead.")
    @Valid
    private final Set<Item> items;

    @JsonProperty("value")
    @Valid
    private final Value value;

    @JsonProperty("minValue")
    @Valid
    private final Value minValue;

    @JsonProperty("procurementMethod")
    @JsonPropertyDescription("Specify tendering method using the [method codelist](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#method). This is a closed codelist. Local method types should be mapped" +
            " to " +
            "this list.")
    @Valid
    private final ProcurementMethod procurementMethod;

    @JsonProperty("procurementMethodDetails")
    @JsonPropertyDescription("Additional detail on the procurement method used. This field may be used to provide the" +
            " local name of the particular procurement method used.")
    private final String procurementMethodDetails;

    @JsonProperty("procurementMethodRationale")
    @JsonPropertyDescription("Rationale for the chosen procurement method. This is especially important to provide a " +
            "justification in the case of limited tenders or direct awards.")
    private final String procurementMethodRationale;

    @JsonProperty("mainProcurementCategory")
    @JsonPropertyDescription("The primary category describing the main object of this contracting process from the " +
            "[procurementCategory](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#procurement-category)" +
            " codelist. This is a closed codelist. Local classifications should be mapped to this list.")
    @Valid
    private final MainProcurementCategory mainProcurementCategory;

    @JsonProperty("additionalProcurementCategories")
    @JsonPropertyDescription("Any additional categories which describe the objects of this contracting process, from " +
            "the [extendedProcurementCategory](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#extended-procurement-category) codelist. This is an open codelist. " +
            "Local " +
            "categories can be included in this list.")
    private final List<ExtendedProcurementCategory> additionalProcurementCategories;

    @JsonProperty("awardCriteria")
    @JsonPropertyDescription("Specify the award criteria for the procurement, using the [award criteria codelist]" +
            "(http://standard.open-contracting.org/latest/en/schema/codelists/#award-criteria)")
    private final AwardCriteria awardCriteria;

    @JsonProperty("awardCriteriaDetails")
    @JsonPropertyDescription("Any detailed or further information on the award or selection criteria.")
    private final String awardCriteriaDetails;

    @JsonProperty("submissionMethod")
    @JsonPropertyDescription("Specify the method by which bids must be submitted, in person, written, or electronic " +
            "auction. Using the [submission method codelist](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#submission-method)")
    private final List<SubmissionMethod> submissionMethod;

    @JsonProperty("submissionMethodDetails")
    @JsonPropertyDescription("Any detailed or further information on the submission method. This may include the " +
            "address, e-mail address or online service to which bids should be submitted, and any special " +
            "requirements to" +
            " be followed for submissions.")
    private final String submissionMethodDetails;

    @JsonProperty("tenderPeriod")
    @Valid
    private final Period tenderPeriod;

    @JsonProperty("enquiryPeriod")
    @Valid
    private final Period enquiryPeriod;

    @JsonProperty("hasEnquiries")
    @JsonPropertyDescription("A true/false field to indicate whether any enquiries were received during the tender " +
            "process. Structured information on enquiries that were received, and responses to them, can be provided " +
            "using the enquiries extension.")
    private final Boolean hasEnquiries;

    @JsonProperty("enquiries")
    @JsonPropertyDescription("Enquiries array to tender, consisting of one or more enquiry objects, each with fields " +
            "for a question, and an answer.")
    private final List<Enquiry> enquiries;

    @JsonProperty("eligibilityCriteria")
    @JsonPropertyDescription("A description of any eligibility criteria for potential suppliers.")
    private final String eligibilityCriteria;

    @JsonProperty("awardPeriod")
    @Valid
    private final Period awardPeriod;

    @JsonProperty("contractPeriod")
    @Valid
    private final Period contractPeriod;

    @JsonProperty("numberOfTenderers")
    @JsonPropertyDescription("The number of parties who submit a bid.")
    private final Integer numberOfTenderers;

    @JsonProperty("tenderers")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("All parties who submit a bid on a tender. More detailed information on bids and the " +
            "bidding organization can be provided using the optional bid extension.")
    @Valid
    private final Set<OrganizationReference> tenderers;

    @JsonProperty("procuringEntity")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
            "section")
    @Valid
    private final OrganizationReference procuringEntity;

    @JsonProperty("documents")
    @JsonPropertyDescription("All documents and attachments related to the tender, including any notices. See the " +
            "[documentType codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#document-type) " +
            "for " +
            "details of potential documents to include. Common documents include official legal notices of tender, " +
            "technical specifications, evaluation criteria, and, as a tender process progresses, clarifications and " +
            "replies to queries.")
    @Valid
    private final List<Document> documents;

    @JsonProperty("milestones")
    @JsonPropertyDescription("A list of milestones associated with the tender.")
    @Valid
    private final List<Milestone> milestones;

    @JsonProperty("amendments")
    @JsonPropertyDescription("A tender amendment is a formal change to the tender, and generally involves the " +
            "publication of a new tender notice/release. The rationale and a description of the changes made can be " +
            "provided here.")
    @Valid
    private final List<Amendment> amendments;

    @JsonProperty("amendment")
    @JsonPropertyDescription("Amendment information")
    @Valid
    private final Amendment amendment;

    @JsonProperty("lots")
    @JsonPropertyDescription("A tender process may be divided into lots, where bidders can bid on one or more lots. " +
            "Details of each lot can be provided here. Items, documents and other features can then reference the lot" +
            " " +
            "they are related to using relatedLot. Where no relatedLot identifier is given, the values should be " +
            "interpreted as applicable to the whole tender. Properties of tender can be overridden for a given Lot " +
            "through their inclusion in the Lot object.")
    @Valid
    private List<Lot> lots;

    @JsonProperty("lotDetails")
    @JsonPropertyDescription("If this tender is divided into lots, details can be provided here of any criteria that " +
            "apply to bidding on these lots. This extended property is currently focussed on fields required by the " +
            "EU " +
            "TED data standard")
    private final LotDetails lotDetails;

    @JsonProperty("lotGroups")
    @JsonPropertyDescription("ere the buyer reserves the right to combine lots, or wishes to specify the total value " +
            "for a group of lots, a lot group is used to capture this information.")
    @Valid
    private final List<LotGroup> lotGroups;

    @JsonProperty("participationFees")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("Any fees applicable to bidders wishing to participate in the tender process. Fees may " +
            "apply for access to bidding documents, for the submission of bids or there may be a win fee payable by " +
            "the " +
            "successful bidder.")
    @Valid
    private final Set<ParticipationFee> participationFees;

    @JsonProperty("criteria")
    @JsonPropertyDescription("A list of criteria on which either bidders or items will be judged, evaluated or " +
            "assessed.")
    @Valid
    private final List<Criterion> criteria;

    @JsonProperty("acceleratedProcedure")
    @Valid
    private final AcceleratedProcedure acceleratedProcedure;

    @JsonProperty("classification")
    @Valid
    private final Classification classification;

    @JsonProperty("designContest")
    @Valid
    private final DesignContest designContest;

    @JsonProperty("electronicWorkflows")
    @Valid
    private final ElectronicWorkflows electronicWorkflows;

    @JsonProperty("jointProcurement")
    @Valid
    private final JointProcurement jointProcurement;

    @JsonProperty("legalBasis")
    @JsonPropertyDescription("The legal basis of the tender based on the [legalBasis codelist](http://standard" +
            ".open-contracting.org/......")
    private final LegalBasis legalBasis;

    @JsonProperty("objectives")
    @Valid
    private final Objectives objectives;

    @JsonProperty("procedureOutsourcing")
    @Valid
    private final ProcedureOutsourcing procedureOutsourcing;

    @JsonProperty("procurementMethodAdditionalInfo")
    @JsonPropertyDescription("Additional information about the procurement method.")
    private final String procurementMethodAdditionalInfo;

    @JsonProperty("reviewParties")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The organizations involved in the review procedure for the procurement procedure. " +
            "Required by the EU and UNCITRAL")
    @Valid
    private final Set<OrganizationReference> reviewParties;

    @JsonProperty("reviewPeriod")
    @Valid
    private final Period reviewPeriod;

    @JsonProperty("standstillPeriod")
    @Valid
    private final Period standstillPeriod;

    @JsonProperty("submissionLanguages")
    @JsonPropertyDescription("Language(s) in which tenderers may submit, drawn from the [submissionLanguages " +
            "codelist](http://standard.open-contracting.org/1.1-dev/en/schema/codelists/#submission-languages)")
    private final List<SubmissionLanguage> submissionLanguages;

    @JsonProperty("submissionMethodRationale")
    @JsonPropertyDescription("A value from the [submissionValueRationale codelist](http://standard.open-contracting" +
            ".org/1.1-dev/en/schema/codelists/submission-method-rationale) that identifies the rationale where " +
            "electronic" +
            " submission method is not to be allowed. Required by EU.")
    @Valid
    private final List<SubmissionMethodRationale> submissionMethodRationale;

    @JsonProperty("dynamicPurchasingSystem")
    @JsonPropertyDescription("Dynamic Purchasing System: Whether a dynamic purchasing system has been set up and if " +
            "so whether it may be used by buyers outside the notice. Required by EU.")
    @Valid
    private final DynamicPurchasingSystem dynamicPurchasingSystem;

    @JsonProperty("framework")
    @JsonPropertyDescription("The details of any framework agreement established as part of this procurement. " +
            "Required by EU.")
    @Valid
    private final Framework framework;

    @JsonProperty("requiresElectronicCatalogue")
    @JsonPropertyDescription("Tenders must include an electronic catalogue. Required by the EU")
    private final Boolean requiresElectronicCatalogue;

    @JsonCreator
    public Tender(@JsonProperty("id") final String id,
                  @JsonProperty("title") final String title,
                  @JsonProperty("description") final String description,
                  @JsonProperty("status") final TenderStatus status,
                  @JsonProperty("statusDetails") final TenderStatusDetails statusDetails,
                  @JsonProperty("items") final LinkedHashSet<Item> items,
                  @JsonProperty("minValue") final Value minValue,
                  @JsonProperty("value") final Value value,
                  @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                  @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                  @JsonProperty("procurementMethodRationale") final String procurementMethodRationale,
                  @JsonProperty("mainProcurementCategory") final MainProcurementCategory mainProcurementCategory,
                  @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategory>
                          additionalProcurementCategories,
                  @JsonProperty("awardCriteria") final AwardCriteria awardCriteria,
                  @JsonProperty("awardCriteriaDetails") final String awardCriteriaDetails,
                  @JsonProperty("submissionMethod") final List<SubmissionMethod> submissionMethod,
                  @JsonProperty("submissionMethodDetails") final String submissionMethodDetails,
                  @JsonProperty("tenderPeriod") final Period tenderPeriod,
                  @JsonProperty("enquiryPeriod") final Period enquiryPeriod,
                  @JsonProperty("hasEnquiries") final Boolean hasEnquiries,
                  @JsonProperty("enquiries") final List<Enquiry> enquiries,
                  @JsonProperty("eligibilityCriteria") final String eligibilityCriteria,
                  @JsonProperty("awardPeriod") final Period awardPeriod,
                  @JsonProperty("contractPeriod") final Period contractPeriod,
                  @JsonProperty("numberOfTenderers") final Integer numberOfTenderers,
                  @JsonProperty("tenderers") final LinkedHashSet<OrganizationReference> tenderers,
                  @JsonProperty("procuringEntity") final OrganizationReference procuringEntity,
                  @JsonProperty("documents") final List<Document> documents,
                  @JsonProperty("milestones") final List<Milestone> milestones,
                  @JsonProperty("amendments") final List<Amendment> amendments,
                  @JsonProperty("amendment") final Amendment amendment,
                  @JsonProperty("lots") final List<Lot> lots,
                  @JsonProperty("lotDetails") final LotDetails lotDetails,
                  @JsonProperty("lotGroups") final List<LotGroup> lotGroups,
                  @JsonProperty("participationFees") final LinkedHashSet<ParticipationFee> participationFees,
                  @JsonProperty("criteria") final List<Criterion> criteria,
                  @JsonProperty("acceleratedProcedure") final AcceleratedProcedure acceleratedProcedure,
                  @JsonProperty("classification") final Classification classification,
                  @JsonProperty("designContest") final DesignContest designContest,
                  @JsonProperty("electronicWorkflows") final ElectronicWorkflows electronicWorkflows,
                  @JsonProperty("jointProcurement") final JointProcurement jointProcurement,
                  @JsonProperty("legalBasis") final LegalBasis legalBasis,
                  @JsonProperty("objectives") final Objectives objectives,
                  @JsonProperty("procedureOutsourcing") final ProcedureOutsourcing procedureOutsourcing,
                  @JsonProperty("procurementMethodAdditionalInfo") final String procurementMethodAdditionalInfo,
                  @JsonProperty("reviewParties") final LinkedHashSet<OrganizationReference> reviewParties,
                  @JsonProperty("reviewPeriod") final Period reviewPeriod,
                  @JsonProperty("standstillPeriod") final Period standstillPeriod,
                  @JsonProperty("submissionLanguages") final List<SubmissionLanguage> submissionLanguages,
                  @JsonProperty("submissionMethodRationale") final List<SubmissionMethodRationale>
                          submissionMethodRationale,
                  @JsonProperty("dynamicPurchasingSystem") final DynamicPurchasingSystem dynamicPurchasingSystem,
                  @JsonProperty("framework") final Framework framework,
                  @JsonProperty("requiresElectronicCatalogue") final Boolean requiresElectronicCatalogue) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.statusDetails = statusDetails;
        this.items = items;
        this.value = value;
        this.minValue = minValue;
        this.procurementMethod = procurementMethod;
        this.procurementMethodDetails = procurementMethodDetails;
        this.procurementMethodRationale = procurementMethodRationale;
        this.mainProcurementCategory = mainProcurementCategory;
        this.additionalProcurementCategories = additionalProcurementCategories;
        this.awardCriteria = awardCriteria;
        this.awardCriteriaDetails = awardCriteriaDetails;
        this.submissionMethod = submissionMethod;
        this.submissionMethodDetails = submissionMethodDetails;
        this.tenderPeriod = tenderPeriod;
        this.enquiryPeriod = enquiryPeriod;
        this.hasEnquiries = hasEnquiries;
        this.enquiries = enquiries;
        this.eligibilityCriteria = eligibilityCriteria;
        this.awardPeriod = awardPeriod;
        this.contractPeriod = contractPeriod;
        this.numberOfTenderers = numberOfTenderers;
        this.tenderers = tenderers;
        this.procuringEntity = procuringEntity;
        this.documents = documents;
        this.milestones = milestones;
        this.amendments = amendments;
        this.amendment = amendment;
        this.lots = lots;
        this.lotDetails = lotDetails;
        this.lotGroups = lotGroups;
        this.participationFees = participationFees;
        this.criteria = criteria;
        this.acceleratedProcedure = acceleratedProcedure;
        this.classification = classification;
        this.designContest = designContest;
        this.electronicWorkflows = electronicWorkflows;
        this.jointProcurement = jointProcurement;
        this.legalBasis = legalBasis;
        this.objectives = objectives;
        this.procedureOutsourcing = procedureOutsourcing;
        this.procurementMethodAdditionalInfo = procurementMethodAdditionalInfo;
        this.reviewParties = reviewParties;
        this.reviewPeriod = reviewPeriod;
        this.standstillPeriod = standstillPeriod;
        this.submissionLanguages = submissionLanguages;
        this.submissionMethodRationale = submissionMethodRationale;
        this.dynamicPurchasingSystem = dynamicPurchasingSystem;
        this.framework = framework;
        this.requiresElectronicCatalogue = requiresElectronicCatalogue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id)
                .append(title)
                .append(description)
                .append(status)
                .append(statusDetails)
                .append(items)
                .append(minValue)
                .append(value)
                .append(procurementMethod)
                .append(procurementMethodDetails)
                .append(procurementMethodRationale)
                .append(mainProcurementCategory)
                .append(additionalProcurementCategories)
                .append(awardCriteria)
                .append(awardCriteriaDetails)
                .append(submissionMethod)
                .append(submissionMethodDetails)
                .append(tenderPeriod)
                .append(enquiryPeriod)
                .append(hasEnquiries)
                .append(enquiries)
                .append(eligibilityCriteria)
                .append(awardPeriod)
                .append(contractPeriod)
                .append(numberOfTenderers)
                .append(tenderers)
                .append(procuringEntity)
                .append(documents)
                .append(milestones)
                .append(amendments)
                .append(amendment)
                .append(lots)
                .append(lotDetails)
                .append(lotGroups)
                .append(participationFees)
                .append(criteria)
                .append(acceleratedProcedure)
                .append(classification)
                .append(designContest)
                .append(electronicWorkflows)
                .append(jointProcurement)
                .append(legalBasis)
                .append(objectives)
                .append(procedureOutsourcing)
                .append(procurementMethodAdditionalInfo)
                .append(reviewParties)
                .append(reviewPeriod)
                .append(standstillPeriod)
                .append(submissionLanguages)
                .append(submissionMethodRationale)
                .append(dynamicPurchasingSystem)
                .append(framework)
                .append(requiresElectronicCatalogue)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Tender)) {
            return false;
        }
        final Tender rhs = (Tender) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .append(statusDetails, rhs.statusDetails)
                .append(items, rhs.items)
                .append(minValue, rhs.minValue)
                .append(value, rhs.value)
                .append(procurementMethod, rhs.procurementMethod)
                .append(procurementMethodDetails, rhs.procurementMethodDetails)
                .append(procurementMethodRationale, rhs.procurementMethodRationale)
                .append(mainProcurementCategory, rhs.mainProcurementCategory)
                .append(additionalProcurementCategories, rhs.additionalProcurementCategories)
                .append(awardCriteria, rhs.awardCriteria)
                .append(awardCriteriaDetails, rhs.awardCriteriaDetails)
                .append(submissionMethod, rhs.submissionMethod)
                .append(submissionMethodDetails, rhs.submissionMethodDetails)
                .append(tenderPeriod, rhs.tenderPeriod)
                .append(enquiryPeriod, rhs.enquiryPeriod)
                .append(hasEnquiries, rhs.hasEnquiries)
                .append(enquiries, rhs.enquiries)
                .append(eligibilityCriteria, rhs.eligibilityCriteria)
                .append(awardPeriod, rhs.awardPeriod)
                .append(contractPeriod, rhs.contractPeriod)
                .append(numberOfTenderers, rhs.numberOfTenderers)
                .append(tenderers, rhs.tenderers)
                .append(procuringEntity, rhs.procuringEntity)
                .append(documents, rhs.documents)
                .append(milestones, rhs.milestones)
                .append(amendments, rhs.amendments)
                .append(amendment, rhs.amendment)
                .append(lots, rhs.lots)
                .append(lotDetails, rhs.lotDetails)
                .append(lotGroups, rhs.lotGroups)
                .append(participationFees, rhs.participationFees)
                .append(criteria, rhs.criteria)
                .append(acceleratedProcedure, rhs.acceleratedProcedure)
                .append(classification, rhs.classification)
                .append(designContest, rhs.designContest)
                .append(electronicWorkflows, rhs.electronicWorkflows)
                .append(jointProcurement, rhs.jointProcurement)
                .append(legalBasis, rhs.legalBasis)
                .append(objectives, rhs.objectives)
                .append(procedureOutsourcing, rhs.procedureOutsourcing)
                .append(procurementMethodAdditionalInfo, rhs.procurementMethodAdditionalInfo)
                .append(reviewParties, rhs.reviewParties)
                .append(reviewPeriod, rhs.reviewPeriod)
                .append(standstillPeriod, rhs.standstillPeriod)
                .append(submissionLanguages, rhs.submissionLanguages)
                .append(submissionMethodRationale, rhs.submissionMethodRationale)
                .append(dynamicPurchasingSystem, rhs.dynamicPurchasingSystem)
                .append(framework, rhs.framework)
                .append(requiresElectronicCatalogue, rhs.requiresElectronicCatalogue)
                .isEquals();
    }

    public enum MainProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services");

        private final static Map<String, MainProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final MainProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        MainProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static MainProcurementCategory fromValue(final String value) {
            final MainProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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

    public enum ProcurementMethod {
        OPEN("open"),
        SELECTIVE("selective"),
        LIMITED("limited"),
        DIRECT("direct");

        private final static Map<String, ProcurementMethod> CONSTANTS = new HashMap<>();

        static {
            for (final ProcurementMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private ProcurementMethod(final String value) {
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

    public enum ExtendedProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services"),
        CONSULTING_SERVICES("consultingServices");

        private final static Map<String, ExtendedProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final ExtendedProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        ExtendedProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtendedProcurementCategory fromValue(final String value) {
            final ExtendedProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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

        private final static Map<String, AwardCriteria> CONSTANTS = new HashMap<>();

        static {
            for (final AwardCriteria c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private AwardCriteria(final String value) {
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

        private final static Map<String, SubmissionMethod> CONSTANTS = new HashMap<>();

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
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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

        private final static Map<String, LegalBasis> CONSTANTS = new HashMap<>();

        static {
            for (final LegalBasis c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private LegalBasis(final String value) {
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

    public enum SubmissionLanguage {
        BG("bg"),
        ES("es"),
        CS("cs"),
        DA("da"),
        DE("de"),
        ET("et"),
        EL("el"),
        EN("en"),
        FR("fr"),
        GA("ga"),
        HR("hr"),
        IT("it"),
        LV("lv"),
        LT("lt"),
        HU("hu"),
        MT("mt"),
        NL("nl"),
        PL("pl"),
        PT("pt"),
        RO("ro"),
        SK("sk"),
        SL("sl"),
        FI("fi"),
        SV("sv");

        private final static Map<String, SubmissionLanguage> CONSTANTS = new HashMap<>();

        static {
            for (final SubmissionLanguage c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        SubmissionLanguage(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionLanguage fromValue(final String value) {
            final SubmissionLanguage constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new IllegalArgumentException(
                        "Unknown enum type " + value + ", Allowed values are " + Arrays.toString(values()));
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

    public enum SubmissionMethodRationale {
        TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
        IPR_ISSUES("IPR_ISSUES"),
        REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
        PHYSICAL_MODEL("PHYSICAL_MODEL"),
        SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

        private final static Map<String, SubmissionMethodRationale> CONSTANTS = new HashMap<>();

        static {
            for (final SubmissionMethodRationale c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private SubmissionMethodRationale(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionMethodRationale fromValue(final String value) {
            final SubmissionMethodRationale constant = CONSTANTS.get(value);
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
