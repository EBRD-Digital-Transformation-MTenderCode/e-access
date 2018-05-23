package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnTender(

        @JsonProperty("id")
        var id: String?,

        @JsonProperty("title") @NotNull
        val title: String,

        @JsonProperty("description") @NotNull
        val description: String,

        @JsonProperty("status")
        var status: TenderStatus?,

        @JsonProperty("statusDetails")
        var statusDetails: TenderStatusDetails?,

        @JsonProperty("classification") @Valid @NotNull
        val classification: Classification,

        @JsonProperty("acceleratedProcedure") @Valid
        val acceleratedProcedure: AcceleratedProcedure?,

        @JsonProperty("designContest") @Valid
        val designContest: DesignContest?,

        @JsonProperty("electronicWorkflows") @Valid
        val electronicWorkflows: PnElectronicWorkflows?,

        @JsonProperty("jointProcurement") @Valid
        val jointProcurement: JointProcurement?,

        @JsonProperty("procedureOutsourcing") @Valid
        val procedureOutsourcing: ProcedureOutsourcing?,

        @JsonProperty("framework") @Valid
        val framework: Framework?,

        @JsonProperty("dynamicPurchasingSystem") @Valid
        val dynamicPurchasingSystem: DynamicPurchasingSystem?,

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

        @JsonProperty("eligibilityCriteria")
        val eligibilityCriteria: String?,

        @JsonProperty("submissionLanguages")
        val submissionLanguages: List<SubmissionLanguage>?,

        @JsonProperty("contractPeriod") @Valid
        val contractPeriod: Period?,

        @JsonProperty("procuringEntity") @Valid @NotNull
        val procuringEntity: OrganizationReference,

        @JsonProperty("value") @Valid @NotNull
        val value: Value,

        @JsonProperty("lotGroups")
        val lotGroups: List<LotGroup?>,

        @JsonProperty("lots")
        val lots: List<PnLot>?,

        @JsonProperty("items")
        val items: LinkedHashSet<PnItem>?,

        @JsonProperty("awardCriteria")
        val awardCriteria: AwardCriteria?,

        @JsonProperty("requiresElectronicCatalogue")
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        @JsonProperty("submissionMethod")
        val submissionMethod: List<SubmissionMethod>?,

        @JsonProperty("submissionMethodDetails")
        val submissionMethodDetails: String?,

        @JsonProperty("submissionMethodRationale")
        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        @JsonProperty("documents") @Valid
        val documents: List<Document>?,

        @JsonProperty("tenderPeriod") @Valid @NotNull
        val tenderPeriod: PnPeriod
)