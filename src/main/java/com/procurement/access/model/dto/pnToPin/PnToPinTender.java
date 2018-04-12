package com.procurement.access.model.dto.pnToPin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.procurement.access.exception.EnumException;
import com.procurement.access.model.dto.ocds.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        "procuringEntity",
        "value",
        "lotGroups",
        "lots",
        "items",
        "awardCriteria",
        "requiresElectronicCatalogue",
        "submissionMethod",
        "submissionMethodRationale",
        "submissionMethodDetails",
        "documents"
})
public class PnToPinTender {

    @NotNull
    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("title")
    private String title;

    @NotNull
    @JsonProperty("description")
    private String description;

    @JsonProperty("status")
    private TenderStatus status;

    @JsonProperty("statusDetails")
    private TenderStatusDetails statusDetails;

    @Valid
    @NotNull
    @JsonProperty("classification")
    private Classification classification;

    @Valid
    @NotNull
    @JsonProperty("acceleratedProcedure")
    private final AcceleratedProcedure acceleratedProcedure;

    @Valid
    @NotNull
    @JsonProperty("designContest")
    private final DesignContest designContest;

    @Valid
    @NotNull
    @JsonProperty("electronicWorkflows")
    private final ElectronicWorkflows electronicWorkflows;

    @Valid
    @NotNull
    @JsonProperty("jointProcurement")
    private final JointProcurement jointProcurement;

    @Valid
    @NotNull
    @JsonProperty("procedureOutsourcing")
    private final ProcedureOutsourcing procedureOutsourcing;

    @Valid
    @NotNull
    @JsonProperty("framework")
    private final Framework framework;

    @Valid
    @NotNull
    @JsonProperty("dynamicPurchasingSystem")
    private final DynamicPurchasingSystem dynamicPurchasingSystem;

    @NotNull
    @JsonProperty("legalBasis")
    private LegalBasis legalBasis;

    @NotNull
    @JsonProperty("procurementMethod")
    private ProcurementMethod procurementMethod;

    @NotNull
    @JsonProperty("procurementMethodDetails")
    private String procurementMethodDetails;

    @JsonProperty("procurementMethodRationale")
    private final String procurementMethodRationale;

    @JsonProperty("procurementMethodAdditionalInfo")
    private final String procurementMethodAdditionalInfo;

    @NotNull
    @JsonProperty("mainProcurementCategory")
    private MainProcurementCategory mainProcurementCategory;

    @Valid
    @JsonProperty("additionalProcurementCategories")
    private final List<ExtendedProcurementCategory> additionalProcurementCategories;

    @Valid
    @JsonProperty("eligibilityCriteria")
    private final String eligibilityCriteria;

    @Valid
    @JsonProperty("submissionLanguages")
    private final List<SubmissionLanguage> submissionLanguages;

    @Valid
    @NotNull
    @JsonProperty("tenderPeriod")
    private PnToPinPeriod tenderPeriod;

    @Valid
    @NotNull
    @JsonProperty("contractPeriod")
    private final Period contractPeriod;

    @Valid
    @NotNull
    @JsonProperty("procuringEntity")
    private PnToPinOrganizationReference procuringEntity;

    @Valid
    @NotNull
    @JsonProperty("value")
    private final Value value;

    @Valid
    @JsonProperty("lotGroups")
    private final List<LotGroup> lotGroups;

    @NotEmpty
    @JsonProperty("lots")
    private List<PnToPinLot> lots;

    @NotEmpty
    @JsonProperty("items")
    private Set<PnToPinItem> items;

    @NotNull
    @JsonProperty("awardCriteria")
    private final AwardCriteria awardCriteria;

    @JsonProperty("requiresElectronicCatalogue")
    private final Boolean requiresElectronicCatalogue;

    @NotEmpty
    @JsonProperty("submissionMethod")
    private final List<SubmissionMethod> submissionMethod;

    @JsonProperty("submissionMethodRationale")
    private final List<SubmissionMethodRationale> submissionMethodRationale;

    @JsonProperty("submissionMethodDetails")
    private final String submissionMethodDetails;

    @JsonProperty("documents")
    private List<PnToPinDocument> documents;



    @JsonCreator
    public PnToPinTender(@JsonProperty("id") final String id,
                         @JsonProperty("title") final String title,
                         @JsonProperty("description") final String description,
                         @JsonProperty("status") final TenderStatus status,
                         @JsonProperty("statusDetails") final TenderStatusDetails statusDetails,
                         @JsonProperty("classification") final Classification classification,
                         @JsonProperty("items") final LinkedHashSet<PnToPinItem> items,
                         @JsonProperty("value") final Value value,
                         @JsonProperty("procurementMethod") final ProcurementMethod procurementMethod,
                         @JsonProperty("procurementMethodDetails") final String procurementMethodDetails,
                         @JsonProperty("procurementMethodRationale") final String procurementMethodRationale,
                         @JsonProperty("mainProcurementCategory") final MainProcurementCategory mainProcurementCategory,
                         @JsonProperty("additionalProcurementCategories") final List<ExtendedProcurementCategory>
                          additionalProcurementCategories,
                         @JsonProperty("awardCriteria") final AwardCriteria awardCriteria,
                         @JsonProperty("submissionMethod") final List<SubmissionMethod> submissionMethod,
                         @JsonProperty("submissionMethodDetails") final String submissionMethodDetails,
                         @JsonProperty("tenderPeriod") final PnToPinPeriod tenderPeriod,
                         @JsonProperty("eligibilityCriteria") final String eligibilityCriteria,
                         @JsonProperty("contractPeriod") final Period contractPeriod,
                         @JsonProperty("procuringEntity") final PnToPinOrganizationReference procuringEntity,
                         @JsonProperty("documents") final List<PnToPinDocument> documents,
                         @JsonProperty("lots") final List<PnToPinLot> lots,
                         @JsonProperty("lotGroups") final List<LotGroup> lotGroups,
                         @JsonProperty("acceleratedProcedure") final AcceleratedProcedure acceleratedProcedure,
                         @JsonProperty("designContest") final DesignContest designContest,
                         @JsonProperty("electronicWorkflows") final ElectronicWorkflows electronicWorkflows,
                         @JsonProperty("jointProcurement") final JointProcurement jointProcurement,
                         @JsonProperty("legalBasis") final LegalBasis legalBasis,
                         @JsonProperty("procedureOutsourcing") final ProcedureOutsourcing procedureOutsourcing,
                         @JsonProperty("procurementMethodAdditionalInfo") final String procurementMethodAdditionalInfo,
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
        this.classification = classification;
        this.items = items;
        this.value = value;
        this.procurementMethod = procurementMethod;
        this.procurementMethodDetails = procurementMethodDetails;
        this.procurementMethodRationale = procurementMethodRationale;
        this.mainProcurementCategory = mainProcurementCategory;
        this.additionalProcurementCategories = additionalProcurementCategories;
        this.awardCriteria = awardCriteria;
        this.submissionMethod = submissionMethod;
        this.submissionMethodDetails = submissionMethodDetails;
        this.tenderPeriod = tenderPeriod;
        this.eligibilityCriteria = eligibilityCriteria;
        this.contractPeriod = contractPeriod;
        this.procuringEntity = procuringEntity;
        this.documents = documents;
        this.lots = lots;
        this.lotGroups = lotGroups;
        this.acceleratedProcedure = acceleratedProcedure;
        this.designContest = designContest;
        this.electronicWorkflows = electronicWorkflows;
        this.jointProcurement = jointProcurement;
        this.legalBasis = legalBasis;
        this.procedureOutsourcing = procedureOutsourcing;
        this.procurementMethodAdditionalInfo = procurementMethodAdditionalInfo;
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
                .append(classification)
                .append(items)
                .append(value)
                .append(procurementMethod)
                .append(procurementMethodDetails)
                .append(procurementMethodRationale)
                .append(mainProcurementCategory)
                .append(additionalProcurementCategories)
                .append(awardCriteria)
                .append(submissionMethod)
                .append(submissionMethodDetails)
                .append(eligibilityCriteria)
                .append(contractPeriod)
                .append(procuringEntity)
                .append(documents)
                .append(lots)
                .append(lotGroups)
                .append(acceleratedProcedure)
                .append(designContest)
                .append(electronicWorkflows)
                .append(jointProcurement)
                .append(legalBasis)
                .append(procedureOutsourcing)
                .append(procurementMethodAdditionalInfo)
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
        if (!(other instanceof PnToPinTender)) {
            return false;
        }
        final PnToPinTender rhs = (PnToPinTender) other;
        return new EqualsBuilder().append(id, rhs.id)
                .append(title, rhs.title)
                .append(description, rhs.description)
                .append(status, rhs.status)
                .append(statusDetails, rhs.statusDetails)
                .append(classification, rhs.classification)
                .append(items, rhs.items)
                .append(value, rhs.value)
                .append(procurementMethod, rhs.procurementMethod)
                .append(procurementMethodDetails, rhs.procurementMethodDetails)
                .append(procurementMethodRationale, rhs.procurementMethodRationale)
                .append(mainProcurementCategory, rhs.mainProcurementCategory)
                .append(additionalProcurementCategories, rhs.additionalProcurementCategories)
                .append(awardCriteria, rhs.awardCriteria)
                .append(submissionMethod, rhs.submissionMethod)
                .append(submissionMethodDetails, rhs.submissionMethodDetails)
                .append(tenderPeriod, rhs.tenderPeriod)
                .append(eligibilityCriteria, rhs.eligibilityCriteria)
                .append(contractPeriod, rhs.contractPeriod)
                .append(procuringEntity, rhs.procuringEntity)
                .append(documents, rhs.documents)
                .append(lots, rhs.lots)
                .append(lotGroups, rhs.lotGroups)
                .append(acceleratedProcedure, rhs.acceleratedProcedure)
                .append(designContest, rhs.designContest)
                .append(electronicWorkflows, rhs.electronicWorkflows)
                .append(jointProcurement, rhs.jointProcurement)
                .append(legalBasis, rhs.legalBasis)
                .append(procedureOutsourcing, rhs.procedureOutsourcing)
                .append(procurementMethodAdditionalInfo, rhs.procurementMethodAdditionalInfo)
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

        private static final Map<String, MainProcurementCategory> CONSTANTS = new HashMap<>();
        private final String value;

        static {
            for (final MainProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        MainProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static MainProcurementCategory fromValue(final String value) {
            final MainProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(MainProcurementCategory.class.getName(), value, Arrays.toString(values()));
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
        private final String value;

        static {
            for (final ProcurementMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ProcurementMethod(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ProcurementMethod fromValue(final String value) {
            final ProcurementMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(ProcurementMethod.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public enum ExtendedProcurementCategory {
        GOODS("goods"),
        WORKS("works"),
        SERVICES("services"),
        CONSULTING_SERVICES("consultingServices");

        private static final Map<String, ExtendedProcurementCategory> CONSTANTS = new HashMap<>();
        private final String value;

        static {
            for (final ExtendedProcurementCategory c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        ExtendedProcurementCategory(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static ExtendedProcurementCategory fromValue(final String value) {
            final ExtendedProcurementCategory constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(ExtendedProcurementCategory.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
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
        private final String value;

        static {
            for (final AwardCriteria c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        AwardCriteria(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static AwardCriteria fromValue(final String value) {
            final AwardCriteria constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(AwardCriteria.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public enum SubmissionMethod {
        ELECTRONIC_SUBMISSION("electronicSubmission"),
        ELECTRONIC_AUCTION("electronicAuction"),
        WRITTEN("written"),
        IN_PERSON("inPerson");

        private static final Map<String, SubmissionMethod> CONSTANTS = new HashMap<>();
        private final String value;

        static {
            for (final SubmissionMethod c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SubmissionMethod(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionMethod fromValue(final String value) {
            final SubmissionMethod constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(SubmissionMethod.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
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
        private final String value;

        static {
            for (final LegalBasis c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        LegalBasis(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static LegalBasis fromValue(final String value) {
            final LegalBasis constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(LegalBasis.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
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
        private final String value;

        static {
            for (final SubmissionLanguage c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SubmissionLanguage(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionLanguage fromValue(final String value) {
            final SubmissionLanguage constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(SubmissionLanguage.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
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
        private final String value;

        static {
            for (final SubmissionMethodRationale c : values()) {
                CONSTANTS.put(c.value, c);
            }
        }

        SubmissionMethodRationale(final String value) {
            this.value = value;
        }

        @JsonCreator
        public static SubmissionMethodRationale fromValue(final String value) {
            final SubmissionMethodRationale constant = CONSTANTS.get(value);
            if (constant == null) {
                throw new EnumException(SubmissionMethodRationale.class.getName(), value, Arrays.toString(values()));
            }
            return constant;
        }

        @JsonValue
        public String value() {
            return this.value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
