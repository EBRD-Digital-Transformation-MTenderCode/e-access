package com.procurement.access.model.dto.ocds

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.ExtendedProcurementCategory
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.SubmissionLanguage
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.model.dto.databinding.BooleansDeserializer

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Tender @JsonCreator constructor(

        val id: String?,

        var title: String,

        var description: String,

        var status: TenderStatus,

        var statusDetails: TenderStatusDetails,

        var classification: Classification,

        val mainProcurementCategory: MainProcurementCategory,

        var additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val procurementMethod: ProcurementMethod,

        val procurementMethodDetails: String,

        var procurementMethodRationale: String?,

        var procurementMethodAdditionalInfo: String?,

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

        @JsonInclude(JsonInclude.Include.NON_NULL)
        val procuringEntity: OrganizationReference?,

        var awardCriteria: AwardCriteria?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("awardCriteriaDetails") @param:JsonProperty("awardCriteriaDetails") val awardCriteriaDetails: AwardCriteriaDetails?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<CNEntity.Tender.Criteria> = emptyList(),

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("conversions") @param:JsonProperty("conversions") val conversions: List<CNEntity.Tender.Conversion> = emptyList(),

        @get:JsonProperty("requiresElectronicCatalogue")
        @field:JsonDeserialize(using = BooleansDeserializer::class)
        val requiresElectronicCatalogue: Boolean,

        var contractPeriod: ContractPeriod?,

        var tenderPeriod: Period? = null,

        var enquiryPeriod: Period? = null,

        var value: Value,

        val lotGroups: List<LotGroup>,

        var lots: List<Lot>,

        var items: List<Item>,

        var documents: List<Document>?,

        var procurementMethodModalities: Set<ProcurementMethodModalities>?,

        var electronicAuctions: ElectronicAuctions?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("otherCriteria") @param:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria? = null
)
