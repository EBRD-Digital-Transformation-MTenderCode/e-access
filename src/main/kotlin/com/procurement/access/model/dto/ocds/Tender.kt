package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Tender @JsonCreator constructor(

        val id: String?,

        val title: String,

        val description: String,

        var status: TenderStatus,

        var statusDetails: TenderStatusDetails,

        val classification: Classification,

        val mainProcurementCategory: MainProcurementCategory,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val procurementMethod: ProcurementMethod,

        val procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val submissionMethod: List<SubmissionMethod>,

        val submissionMethodDetails: String,

        val submissionMethodRationale: List<String>,

        val submissionLanguages: List<SubmissionLanguage>?,

        val eligibilityCriteria: String,

        val acceleratedProcedure: AcceleratedProcedure,

        val designContest: DesignContest,

        val electronicWorkflows: ElectronicWorkflows,

        val jointProcurement: JointProcurement,

        val procedureOutsourcing: ProcedureOutsourcing,

        val framework: Framework,

        val dynamicPurchasingSystem: DynamicPurchasingSystem,

        val legalBasis: LegalBasis,

        val procuringEntity: OrganizationReference,

        val awardCriteria: AwardCriteria,

        @get:JsonProperty("requiresElectronicCatalogue")
        @field:JsonDeserialize(using = BooleansDeserializer::class)
        val requiresElectronicCatalogue: Boolean,

        val contractPeriod: Period,

        val tenderPeriod: Period,

        val value: Value,

        val lotGroups: List<LotGroup>,

        val lots: List<Lot>,

        val items: List<Item>,

        val documents: List<Document>?
)
