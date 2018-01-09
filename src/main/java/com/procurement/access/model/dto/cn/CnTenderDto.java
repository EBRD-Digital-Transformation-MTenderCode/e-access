package com.procurement.access.model.dto.cn;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.*;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "title",
        "description",
        "status",
        "classification",
        "acceleratedProcedure",
        "designContest",
        "electronicWorkflows",
        "jointProcurement",
        "procedureOutsourcing",
        "framework",
        "dynamicPurchasingSystem",
        "legalBasis",
        "procurementMethod",
        "procurementMethodDetails",
        "procurementMethodRationale",
        "procurementMethodAdditionalInfo",
        "mainProcurementCategory",
        "additionalProcurementCategories",
        "eligibilityCriteria",
        "submissionLanguages",
        "tenderPeriod",
        "contractPeriod",
        "value",
        "lotGroups",
        "lots",
        "items",
        "awardCriteria",
        "requiresElectronicCatalogue",
        "submissionMethod",
        "submissionMethodRationale",
        "submissionMethodDetails",
        "procuringEntity",
        "documents"
})
public class CnTenderDto {

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
    private CnTenderStatusDto status;
    @JsonProperty("classification")
    @Valid
    private final CnClassificationDto classification;
    @JsonProperty("acceleratedProcedure")
    @Valid
    private final CnAcceleratedProcedureDto acceleratedProcedure;
    @JsonProperty("designContest")
    @Valid
    private final CnDesignContestDto designContest;
    @JsonProperty("electronicWorkflows")
    @Valid
    private final CnElectronicWorkflowsDto electronicWorkflows;
    @JsonProperty("jointProcurement")
    @Valid
    private final CnJointProcurementDto jointProcurement;
    @JsonProperty("procedureOutsourcing")
    @Valid
    private final CnProcedureOutsourcingDto procedureOutsourcing;
    @JsonProperty("framework")
    @JsonPropertyDescription("The details of any framework agreement established as part of this procurement. " +
            "Required by EU.")
    @Valid
    private final CnFrameworkDto framework;
    @JsonProperty("dynamicPurchasingSystem")
    @JsonPropertyDescription("Dynamic Purchasing System: Whether a dynamic purchasing system has been set up and if " +
            "so whether it may be used by buyers outside the notice. Required by EU.")
    @Valid
    private final CnDynamicPurchasingSystemDto dynamicPurchasingSystem;
    @JsonProperty("legalBasis")
    @JsonPropertyDescription("The legal basis of the tender based on the [legalBasis codelist](http://standard" +
            ".open-contracting.org/......")
    private final LegalBasis legalBasis;
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
    @JsonProperty("procurementMethodAdditionalInfo")
    @JsonPropertyDescription("Additional information about the procurement method.")
    private final String procurementMethodAdditionalInfo;
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
    @JsonProperty("eligibilityCriteria")
    @JsonPropertyDescription("A description of any eligibility criteria for potential suppliers.")
    private final String eligibilityCriteria;
    @JsonProperty("submissionLanguages")
    @JsonPropertyDescription("Language(s) in which tenderers may submit, drawn from the [submissionLanguages " +
            "codelist](http://standard.open-contracting.org/1.1-dev/en/schema/codelists/#submission-languages)")
    private final List<SubmissionLanguage> submissionLanguages;
    @JsonProperty("tenderPeriod")
    @Valid
    private final CnPeriodDto tenderPeriod;
    @JsonProperty("contractPeriod")
    @Valid
    private final CnPeriodDto contractPeriod;
    @JsonProperty("value")
    @Valid
    private final CnValueDto value;
    @JsonProperty("lotGroups")
    @JsonPropertyDescription("ere the buyer reserves the right to combine lots, or wishes to specify the total value " +
            "for a group of lots, a lot group is used to capture this information.")
    @Valid
    private final List<CnLotGroupDto> lotGroups;
    @JsonProperty("lots")
    @JsonPropertyDescription("A tender process may be divided into lots, where bidders can bid on one or more lots. " +
            "Details of each lot can be provided here. Items, documents and other features can then reference the lot" +
            " " +
            "they are related to using relatedLot. Where no relatedLot identifier is given, the values should be " +
            "interpreted as applicable to the whole tender. Properties of tender can be overridden for a given Lot " +
            "through their inclusion in the Lot object.")
    @Valid
    private final List<CnLotDto> lots;
    @JsonProperty("items")
    @JsonDeserialize(as = LinkedHashSet.class)
    @JsonPropertyDescription("The goods and services to be purchased, broken into line items wherever possible. Items" +
            " should not be duplicated, but a quantity of 2 specified instead.")
    @Valid
    private final Set<CnItemDto> items;
    @JsonProperty("awardCriteria")
    @JsonPropertyDescription("Specify the award criteria for the procurement, using the [award criteria codelist]" +
            "(http://standard.open-contracting.org/latest/en/schema/codelists/#award-criteria)")
    private final AwardCriteria awardCriteria;
    @JsonProperty("requiresElectronicCatalogue")
    @JsonPropertyDescription("Tenders must include an electronic catalogue. Required by the EU")
    private final Boolean requiresElectronicCatalogue;
    @JsonProperty("submissionMethod")
    @JsonPropertyDescription("Specify the method by which bids must be submitted, in person, written, or electronic " +
            "auction. Using the [submission method codelist](http://standard.open-contracting" +
            ".org/latest/en/schema/codelists/#submission-method)")
    private final List<SubmissionMethod> submissionMethod;
    @JsonProperty("submissionMethodRationale")
    @JsonPropertyDescription("A value from the [submissionValueRationale codelist](http://standard.open-contracting" +
            ".org/1.1-dev/en/schema/codelists/submission-method-rationale) that identifies the rationale where " +
            "electronic" +
            " submission method is not to be allowed. Required by EU.")
    @Valid
    private final List<SubmissionMethodRationale> submissionMethodRationale;
    @JsonProperty("submissionMethodDetails")
    @JsonPropertyDescription("Any detailed or further information on the submission method. This may include the " +
            "address, e-mail address or online service to which bids should be submitted, and any special " +
            "requirements to" +
            " be followed for submissions.")
    private final String submissionMethodDetails;
    @JsonProperty("procuringEntity")
    @JsonPropertyDescription("The id and name of the party being referenced. Used to cross-reference to the parties " +
            "section")
    @Valid
    private final CnOrganizationReferenceDto procuringEntity;
    @JsonProperty("documents")
    @JsonPropertyDescription("All documents and attachments related to the tender, including any notices. See the " +
            "[documentType codelist](http://standard.open-contracting.org/latest/en/schema/codelists/#document-type) " +
            "for " +
            "details of potential documents to include. Common documents include official legal notices of tender, " +
            "technical specifications, evaluation criteria, and, as a tender process progresses, clarifications and " +
            "replies to queries.")
    @Valid
    private final List<CnDocumentDto> documents;
    @JsonProperty("id")
    @JsonPropertyDescription("An identifier for this tender process. This may be the same as the ocid, or may be " +
            "drawn from an internally held identifier for this tender.")
    private String id;

    @JsonCreator
    public CnTenderDto(@JsonProperty("id") final String id,
                       @JsonProperty("title") final String title,
                       @JsonProperty("description") final String description,
                       @JsonProperty("status") final CnTenderStatusDto status,
                       @JsonProperty("classification") final CnClassificationDto classification,
                       @JsonProperty("acceleratedProcedure") final CnAcceleratedProcedureDto acceleratedProcedure,
                       @JsonProperty("designContest") final CnDesignContestDto designContest,
                       @JsonProperty("electronicWorkflows") final CnElectronicWorkflowsDto electronicWorkflows,
                       @JsonProperty("jointProcurement") final CnJointProcurementDto jointProcurement,
                       @JsonProperty("procedureOutsourcing") final CnProcedureOutsourcingDto procedureOutsourcing,
                       @JsonProperty("framework") final CnFrameworkDto framework,
                       @JsonProperty("dynamicPurchasingSystem") final CnDynamicPurchasingSystemDto
                               dynamicPurchasingSystem,
                       @JsonProperty("legalBasis") final LegalBasis legalBasis,
                       @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                       @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                       @JsonProperty("procurementMethodRationale") final String procurementMethodRationale,
                       @JsonProperty("procurementMethodAdditionalInfo") final String procurementMethodAdditionalInfo,
                       @JsonProperty("mainProcurementCategory") final MainProcurementCategory mainProcurementCategory,
                       @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategory>
                               additionalProcurementCategories,
                       @JsonProperty("eligibilityCriteria") final String eligibilityCriteria,
                       @JsonProperty("submissionLanguages") final List<SubmissionLanguage> submissionLanguages,
                       @JsonProperty("tenderPeriod") final CnPeriodDto tenderPeriod,
                       @JsonProperty("contractPeriod") final CnPeriodDto contractPeriod,
                       @JsonProperty("value") final CnValueDto value,
                       @JsonProperty("lotGroups") final List<CnLotGroupDto> lotGroups,
                       @JsonProperty("lots") final List<CnLotDto> lots,
                       @JsonProperty("items") final Set<CnItemDto> items,
                       @JsonProperty("awardCriteria") final AwardCriteria awardCriteria,
                       @JsonProperty("requiresElectronicCatalogue") final Boolean requiresElectronicCatalogue,
                       @JsonProperty("submissionMethod") final List<SubmissionMethod> submissionMethod,
                       @JsonProperty("submissionMethodRationale") final List<SubmissionMethodRationale>
                               submissionMethodRationale,
                       @JsonProperty("submissionMethodDetails") final String submissionMethodDetails,
                       @JsonProperty("procuringEntity") final CnOrganizationReferenceDto procuringEntity,
                       @JsonProperty("documents") final List<CnDocumentDto> documents) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.classification = classification;
        this.acceleratedProcedure = acceleratedProcedure;
        this.designContest = designContest;
        this.electronicWorkflows = electronicWorkflows;
        this.jointProcurement = jointProcurement;
        this.procedureOutsourcing = procedureOutsourcing;
        this.framework = framework;
        this.dynamicPurchasingSystem = dynamicPurchasingSystem;
        this.legalBasis = legalBasis;
        this.procurementMethod = procurementMethod;
        this.procurementMethodDetails = procurementMethodDetails;
        this.procurementMethodRationale = procurementMethodRationale;
        this.procurementMethodAdditionalInfo = procurementMethodAdditionalInfo;
        this.mainProcurementCategory = mainProcurementCategory;
        this.additionalProcurementCategories = additionalProcurementCategories;
        this.eligibilityCriteria = eligibilityCriteria;
        this.submissionLanguages = submissionLanguages;
        this.tenderPeriod = tenderPeriod;
        this.contractPeriod = contractPeriod;
        this.value = value;
        this.lotGroups = lotGroups;
        this.lots = lots;
        this.items = items;
        this.awardCriteria = awardCriteria;
        this.requiresElectronicCatalogue = requiresElectronicCatalogue;
        this.submissionMethod = submissionMethod;
        this.submissionMethodRationale = submissionMethodRationale;
        this.submissionMethodDetails = submissionMethodDetails;
        this.procuringEntity = procuringEntity;
        this.documents = documents;
    }


    public enum MainProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services");

        private static final Map<String, MainProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final MainProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private MainProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static MainProcurementCategory fromValue(final String value) {
            final MainProcurementCategory constant = CONSTANTS.get(value);
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

        private static final Map<String, ExtendedProcurementCategory> CONSTANTS = new HashMap<>();

        static {
            for (final ExtendedProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private ExtendedProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtendedProcurementCategory fromValue(final String value) {
            final ExtendedProcurementCategory constant = CONSTANTS.get(value);
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

        private static final Map<String, SubmissionMethod> CONSTANTS = new HashMap<>();

        static {
            for (final SubmissionMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private SubmissionMethod(final String value) {
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

        private static final Map<String, SubmissionLanguage> CONSTANTS = new HashMap<>();

        static {
            for (final SubmissionLanguage c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        private final String value;

        private SubmissionLanguage(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionLanguage fromValue(final String value) {
            final SubmissionLanguage constant = CONSTANTS.get(value);
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

    public enum SubmissionMethodRationale {
        TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE("TOOLS_DEVICES_FILE_FORMATS_UNAVAILABLE"),
        IPR_ISSUES("IPR_ISSUES"),
        REQUIRES_SPECIALISED_EQUIPMENT("REQUIRES_SPECIALISED_EQUIPMENT"),
        PHYSICAL_MODEL("PHYSICAL_MODEL"),
        SENSITIVE_INFORMATION("SENSITIVE_INFORMATION");

        private static final Map<String, SubmissionMethodRationale> CONSTANTS = new HashMap<>();

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
