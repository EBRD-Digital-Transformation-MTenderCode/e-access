package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder("id", "title", "description", "status", "statusDetails", "classification", "acceleratedProcedure", "designContest", "electronicWorkflows", "jointProcurement", "procedureOutsourcing", "framework", "dynamicPurchasingSystem", "legalBasis", "procurementMethod", "procurementMethodDetails", "procurementMethodRationale", "procurementMethodAdditionalInfo", "mainProcurementCategory", "additionalProcurementCategories", "eligibilityCriteria", "contractPeriod", "procuringEntity", "value", "lotGroups", "lots", "items", "awardCriteria", "requiresElectronicCatalogue", "submissionMethod", "submissionMethodRationale", "submissionMethodDetails", "documents", "tenderPeriod")
data class Tender(

        @JsonProperty("id")
        val id: String?,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("description")
        val description: String,

        @JsonProperty("status")
        var status: TenderStatus?,

        @JsonProperty("statusDetails")
        var statusDetails: TenderStatusDetails?,

        @JsonProperty("classification") @Valid
        val classification: Classification,

        @JsonProperty("items") @NotEmpty
        val items: HashSet<Item>,

        @JsonProperty("value") @Valid
        val value: Value,

        @JsonProperty("procurementMethod")
        val procurementMethod: ProcurementMethod,

        @JsonProperty("procurementMethodDetails")
        val procurementMethodDetails: String,

        @JsonProperty("procurementMethodRationale")
        val procurementMethodRationale: String?,

        @JsonProperty("mainProcurementCategory")
        val mainProcurementCategory: MainProcurementCategory,

        @JsonProperty("additionalProcurementCategories")
        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        @JsonProperty("awardCriteria")
        val awardCriteria: AwardCriteria,

        @JsonProperty("submissionMethod") @NotEmpty
        val submissionMethod: List<SubmissionMethod>,

        @JsonProperty("submissionMethodDetails")
        val submissionMethodDetails: String?,

        @JsonProperty("tenderPeriod")
        val tenderPeriod: Period?,

        @JsonProperty("eligibilityCriteria")
        val eligibilityCriteria: String,

        @JsonProperty("contractPeriod") @Valid
        val contractPeriod: Period,

        @JsonProperty("procuringEntity") @Valid
        val procuringEntity: OrganizationReference,

        @JsonProperty("documents") @Valid
        val documents: List<Document>?,

        @JsonProperty("lots") @NotEmpty
        val lots: List<Lot>,

        @JsonProperty("lotGroups") @NotEmpty
        val lotGroups: List<LotGroup>,

        @JsonProperty("acceleratedProcedure") @Valid
        val acceleratedProcedure: AcceleratedProcedure,

        @JsonProperty("designContest") @Valid
        val designContest: DesignContest,

        @JsonProperty("electronicWorkflows") @Valid
        val electronicWorkflows: ElectronicWorkflows,

        @JsonProperty("jointProcurement") @Valid
        val jointProcurement: JointProcurement,

        @JsonProperty("legalBasis")
        val legalBasis: LegalBasis,

        @JsonProperty("procedureOutsourcing") @Valid
        val procedureOutsourcing: ProcedureOutsourcing,

        @JsonProperty("procurementMethodAdditionalInfo")
        val procurementMethodAdditionalInfo: String?,

        @JsonProperty("submissionLanguages") @NotEmpty
        val submissionLanguages: List<SubmissionLanguage>,

        @JsonProperty("submissionMethodRationale")
        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        @JsonProperty("dynamicPurchasingSystem") @Valid
        val dynamicPurchasingSystem: DynamicPurchasingSystem,

        @JsonProperty("framework") @Valid
        val framework: Framework,

        @JsonProperty("requiresElectronicCatalogue")
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean
)
