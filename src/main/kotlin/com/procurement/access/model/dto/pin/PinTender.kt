package com.procurement.access.model.dto.pin

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PinTender @JsonCreator constructor(

        var id: String?,

        var title: String,

        var description: String,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        var classification: Classification,

        val acceleratedProcedure: AcceleratedProcedure,

        val designContest: DesignContest,

        val electronicWorkflows: ElectronicWorkflows,

        val jointProcurement: JointProcurement,

        val procedureOutsourcing: ProcedureOutsourcing,

        val framework: Framework,

        val dynamicPurchasingSystem: DynamicPurchasingSystem,

        var legalBasis: LegalBasis,

        var procurementMethod: ProcurementMethod,

        var procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        var mainProcurementCategory: String,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val eligibilityCriteria: String?,

        val submissionLanguages: List<SubmissionLanguage>?,

        val contractPeriod: Period,

        var procuringEntity: OrganizationReference,

        val value: Value,

        val lotGroups: List<LotGroup>?,

        var lots: List<PinLot>?,

        val items: HashSet<Item>?,

        val awardCriteria: AwardCriteria,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        val submissionMethod: List<SubmissionMethod>,

        val submissionMethodDetails: String?,

        val submissionMethodRationale: List<String>?,

        val documents: List<Document>?,

        val tenderPeriod: PinPeriod
)