package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Tender @JsonCreator constructor(

        val id: String?,

        val title: String,

        @field:NotNull
        val description: String,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid @field:NotNull
        val classification: Classification,

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

        @field:NotNull
        val eligibilityCriteria: String,

        @field:NotEmpty
        val submissionLanguages: List<SubmissionLanguage>,

        @field:Valid @field:NotNull
        val contractPeriod: Period,

        @field:Valid @field:NotNull
        val procuringEntity: OrganizationReference,

        @field:Valid @field:NotNull
        val value: Value,

        @field:Valid @field:NotEmpty
        val lotGroups: List<LotGroup>,

        @field:Valid @field:NotEmpty
        var lots: List<Lot>,

        @field:Valid @field:NotEmpty
        var items: HashSet<Item>?,

        @field:NotNull
        val awardCriteria: AwardCriteria,

        @field:NotNull
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean,

        @field:NotEmpty
        val submissionMethod: List<SubmissionMethod>,

        val submissionMethodRationale: List<SubmissionMethodRationale>?,

        val submissionMethodDetails: String?,

        @field:Valid
        var documents: List<Document>?,

        val tenderPeriod: Period?
)
