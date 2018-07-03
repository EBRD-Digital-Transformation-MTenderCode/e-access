package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PnTender @JsonCreator constructor(

        var id: String?,

        @field:NotNull
        val title: String,

        @field:NotNull
        val description: String,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid @field:NotNull
        val classification: Classification,

        @field:Valid
        val acceleratedProcedure: AcceleratedProcedure?,

        @field:Valid
        val designContest: DesignContest?,

        @field:Valid
        val electronicWorkflows: PnElectronicWorkflows?,

        @field:Valid
        val jointProcurement: JointProcurement?,

        @field:Valid
        val procedureOutsourcing: ProcedureOutsourcing?,

        @field:Valid
        val framework: Framework?,

        @field:Valid
        val dynamicPurchasingSystem: DynamicPurchasingSystem?,

        @field:NotNull
        val legalBasis: LegalBasis,

        @field:NotNull
        val procurementMethod: ProcurementMethod,

        @field:NotNull
        val procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        @field:NotNull
        val mainProcurementCategory: MainProcurementCategory,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val eligibilityCriteria: String?,

        val submissionLanguages: List<SubmissionLanguage>?,

        @field:Valid
        val contractPeriod: Period?,

        @field:Valid @field:NotNull
        val procuringEntity: OrganizationReference,

        @field:Valid @field:NotNull
        val value: Value,

        val lotGroups: List<LotGroup?>,

        val lots: List<PnLot>?,

        val items: LinkedHashSet<PnItem>?,

        val awardCriteria: AwardCriteria?,

        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        val submissionMethod: List<SubmissionMethod>?,

        val submissionMethodDetails: String?,

        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        @field:Valid
        val documents: List<Document>?,

        @field:Valid @field:NotNull
        val tenderPeriod: PnPeriod
)