package com.procurement.access.model.dto.pn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import com.procurement.access.model.dto.ocds.Unit
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.HashSet
import java.util.LinkedHashSet
import javax.validation.Valid
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Pn @JsonCreator constructor(

        var ocid: String?,

        var token: String?,

        val planning: Planning,

        val tender: TenderPn
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderPn @JsonCreator constructor(

        var id: String?,

        val title: String,

        val description: String,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        val classification: Classification,

        val acceleratedProcedure: AcceleratedProcedure?,

        val designContest: DesignContest?,

        val electronicWorkflows: ElectronicWorkflows?,

        val jointProcurement: JointProcurement?,

        val procedureOutsourcing: ProcedureOutsourcing?,

        val framework: Framework?,

        val dynamicPurchasingSystem: DynamicPurchasingSystem?,

        val legalBasis: LegalBasis,

        val procurementMethod: ProcurementMethod,

        val procurementMethodDetails: String,

        val procurementMethodRationale: String?,

        val procurementMethodAdditionalInfo: String?,

        val mainProcurementCategory: String,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        val eligibilityCriteria: String?,

        val submissionLanguages: List<SubmissionLanguage>?,

        val contractPeriod: Period?,

        val tenderPeriod: Period,

        val procuringEntity: OrganizationReference,

        val value: Value,

        val lotGroups: List<LotGroup?>,

        val lots: List<LotPn>?,

        val items: HashSet<Item>?,

        val awardCriteria: AwardCriteria?,

        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        val submissionMethod: List<SubmissionMethod>?,

        val submissionMethodDetails: String?,

        val submissionMethodRationale: List<String>?,

        val documents: List<Document>?
)


@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotPn @JsonCreator constructor(

        var id: String,

        val title: String?,

        val description: String?,

        var status: TenderStatus,

        var statusDetails: TenderStatusDetails,

        val value: Value,

        val contractPeriod: Period,

        val placeOfPerformance: PlaceOfPerformance,

        val options: List<Option>?,

        val variants: List<Variant>?,

        val renewals: List<Renewal>?,

        val recurrentProcurement: List<RecurrentProcurement>?
)