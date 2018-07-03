package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.model.dto.ocds.*
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinTender @JsonCreator constructor(

        var id: String?,

        @field:NotNull
        var title: String,

        @field:NotNull
        var description: String,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid @field:NotNull
        var classification: Classification,

        @field:Valid @field:NotNull
        val acceleratedProcedure: AcceleratedProcedure,

        @field:Valid @field:NotNull
        val designContest: DesignContest,

        @field:Valid @field:NotNull
        val electronicWorkflows: ElectronicWorkflows,

        @field:Valid @field:NotNull
        val jointProcurement: JointProcurement,

        @field:Valid @field:NotNull
        val procedureOutsourcing: ProcedureOutsourcing,

        @field:Valid @field:NotNull
        val framework: Framework,

        @field:Valid @field:NotNull
        val dynamicPurchasingSystem: DynamicPurchasingSystem,

        @field:NotNull
        var legalBasis: LegalBasis,

        @field:NotNull
        var procurementMethod: ProcurementMethod,

        @field:NotNull
        var procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        @field:NotNull
        var mainProcurementCategory: MainProcurementCategory,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val eligibilityCriteria: String?,

        val submissionLanguages: List<SubmissionLanguage>?,

        @field:Valid @field:NotNull
        val contractPeriod: Period,

        @field:Valid @field:NotNull
        var procuringEntity: OrganizationReference,

        @field:Valid @field:NotNull
        val value: Value,

        val lotGroups: List<LotGroup>?,

        @field:Valid @field:NotEmpty
        var lots: List<PinLot>?,

        @field:Valid @field:NotEmpty
        val items: HashSet<Item>?,

        @field:NotNull
        val awardCriteria: AwardCriteria,

        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        @field:Valid @field:NotEmpty
        val submissionMethod: List<SubmissionMethod>,

        val submissionMethodDetails: String?,

        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        @field:Valid
        val documents: List<Document>?,

        @field:Valid @field:NotNull
        val tenderPeriod: PinPeriod
)