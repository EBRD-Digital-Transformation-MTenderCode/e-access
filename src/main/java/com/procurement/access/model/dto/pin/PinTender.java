package com.procurement.access.model.dto.pin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.procurement.access.model.dto.ocds.*;
import java.util.LinkedHashSet;
import java.util.List;
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
        "documents",
        "tenderPeriod"
})
public class PinTender {

    @JsonProperty("id")
    private String id;

    @NotNull
    @JsonProperty("title")
    private final String title;

    @NotNull
    @JsonProperty("description")
    private final String description;

    @JsonProperty("status")
    private TenderStatus status;

    @JsonProperty("statusDetails")
    private TenderStatusDetails statusDetails;

    @Valid
    @NotNull
    @JsonProperty("classification")
    private final Classification classification;

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
    private final LegalBasis legalBasis;

    @NotNull
    @JsonProperty("procurementMethod")
    private final ProcurementMethod procurementMethod;

    @NotNull
    @JsonProperty("procurementMethodDetails")
    private final String procurementMethodDetails;

    @JsonProperty("procurementMethodRationale")
    private final String procurementMethodRationale;

    @JsonProperty("procurementMethodAdditionalInfo")
    private final String procurementMethodAdditionalInfo;

    @NotNull
    @JsonProperty("mainProcurementCategory")
    private final MainProcurementCategory mainProcurementCategory;

    @JsonProperty("additionalProcurementCategories")
    private final List<ExtendedProcurementCategory> additionalProcurementCategories;

    @JsonProperty("eligibilityCriteria")
    private final String eligibilityCriteria;

    @JsonProperty("submissionLanguages")
    private final List<SubmissionLanguage> submissionLanguages;

    @Valid
    @NotNull
    @JsonProperty("contractPeriod")
    private final Period contractPeriod;

    @Valid
    @NotNull
    @JsonProperty("procuringEntity")
    private final OrganizationReference procuringEntity;

    @Valid
    @NotNull
    @JsonProperty("value")
    private final Value value;

    @JsonProperty("lotGroups")
    private final List<LotGroup> lotGroups;

    @NotEmpty
    @JsonProperty("lots")
    private List<PinLot> lots;

    @NotEmpty
    @JsonProperty("items")
    private Set<Item> items;

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
    private List<Document> documents;

    @JsonProperty("tenderPeriod")
    private final PinPeriod tenderPeriod;


    @JsonCreator
    public PinTender(@JsonProperty("id") final String id,
                     @JsonProperty("title") final String title,
                     @JsonProperty("description") final String description,
                     @JsonProperty("status") final TenderStatus status,
                     @JsonProperty("statusDetails") final TenderStatusDetails statusDetails,
                     @JsonProperty("classification") final Classification classification,
                     @JsonProperty("items") final LinkedHashSet<Item> items,
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
                     @JsonProperty("eligibilityCriteria") final String eligibilityCriteria,
                     @JsonProperty("contractPeriod") final Period contractPeriod,
                     @JsonProperty("procuringEntity") final OrganizationReference procuringEntity,
                     @JsonProperty("documents") final List<Document> documents,
                     @JsonProperty("lots") final List<PinLot> lots,
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
                     @JsonProperty("requiresElectronicCatalogue") final Boolean requiresElectronicCatalogue,
                     @JsonProperty("tenderPeriod") final PinPeriod tenderPeriod) {
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
        if (!(other instanceof PinTender)) {
            return false;
        }
        final PinTender rhs = (PinTender) other;
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
}
