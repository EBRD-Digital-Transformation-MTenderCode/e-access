package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Tender(

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("description") @NotNull
        val description: String,

        @JsonProperty("status")
        var status: TenderStatus?,

        @JsonProperty("statusDetails")
        var statusDetails: TenderStatusDetails?,

        @JsonProperty("classification") @Valid @NotNull
        val classification: Classification,

        @JsonProperty("acceleratedProcedure") @Valid @NotNull
        val acceleratedProcedure: AcceleratedProcedure,

        @JsonProperty("designContest") @Valid @NotNull
        val designContest: DesignContest,

        @JsonProperty("electronicWorkflows") @Valid @NotNull
        val electronicWorkflows: ElectronicWorkflows,

        @JsonProperty("jointProcurement") @Valid @NotNull
        val jointProcurement: JointProcurement,

        @JsonProperty("procedureOutsourcing") @Valid @NotNull
        val procedureOutsourcing: ProcedureOutsourcing,

        @JsonProperty("framework") @Valid @NotNull
        val framework: Framework,

        @JsonProperty("dynamicPurchasingSystem") @Valid @NotNull
        val dynamicPurchasingSystem: DynamicPurchasingSystem,

        @JsonProperty("legalBasis") @NotNull
        val legalBasis: LegalBasis,

        @JsonProperty("procurementMethod") @NotNull
        val procurementMethod: ProcurementMethod,

        @JsonProperty("procurementMethodDetails") @NotNull
        val procurementMethodDetails: String,

        @JsonProperty("procurementMethodRationale")
        val procurementMethodRationale: String?,

        @JsonProperty("procurementMethodAdditionalInfo")
        val procurementMethodAdditionalInfo: String?,

        @JsonProperty("mainProcurementCategory") @NotNull
        val mainProcurementCategory: MainProcurementCategory,

        @JsonProperty("additionalProcurementCategories")
        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        @JsonProperty("eligibilityCriteria") @NotNull
        val eligibilityCriteria: String,

        @JsonProperty("submissionLanguages") @NotEmpty
        val submissionLanguages: List<SubmissionLanguage>,

        @JsonProperty("contractPeriod") @Valid @NotNull
        val contractPeriod: Period,

        @JsonProperty("procuringEntity") @Valid @NotNull
        val procuringEntity: OrganizationReference,

        @JsonProperty("value") @Valid @NotNull
        val value: Value,

        @JsonProperty("lotGroups") @Valid @NotEmpty
        val lotGroups: List<LotGroup>,

        @JsonProperty("lots") @Valid @NotEmpty
        var lots: List<Lot>,

        @JsonProperty("items") @Valid @NotEmpty
        var items: HashSet<Item>?,

        @JsonProperty("awardCriteria") @NotNull
        val awardCriteria: AwardCriteria,

        @JsonProperty("requiresElectronicCatalogue") @NotNull
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean,

        @JsonProperty("submissionMethod") @NotEmpty
        val submissionMethod: List<SubmissionMethod>,

        @JsonProperty("submissionMethodRationale")
        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        @JsonProperty("submissionMethodDetails")
        val submissionMethodDetails: String?,

        @JsonProperty("documents") @Valid
        var documents: List<Document>?,

        @JsonProperty("tenderPeriod")
        val tenderPeriod: Period?
)
