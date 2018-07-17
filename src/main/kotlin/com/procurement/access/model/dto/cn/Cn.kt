package com.procurement.access.model.dto.cn

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.procurement.access.model.dto.databinding.BooleansDeserializer
import com.procurement.access.model.dto.ocds.*
import java.util.HashSet
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Cn @JsonCreator constructor(

        var ocid: String?,

        var token: String?,

        @field:Valid @field:NotNull
        var planning: Planning,

        @field:Valid @field:NotNull
        var tender: TenderCn
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TenderCn @JsonCreator constructor(

        var id: String?,

        @field:NotNull
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

        val mainProcurementCategory: String,

        val additionalProcurementCategories: List<ExtendedProcurementCategory>?,

        @field:NotNull
        val eligibilityCriteria: String?,

        @field:NotEmpty
        var submissionLanguages: List<SubmissionLanguage>?,

        @field:Valid @field:NotNull
        val contractPeriod: Period,

        @field:Valid @field:NotNull
        val procuringEntity: OrganizationReference,

        @field:Valid @field:NotNull
        val value: Value,

        @field:Valid @field:NotEmpty
        val lotGroups: List<LotGroup>?,

        @field:Valid @field:NotEmpty
        var lots: HashSet<LotCn>?,

        @field:Valid @field:NotEmpty
        val items: HashSet<Item>?,

        @field:NotNull
        val awardCriteria: AwardCriteria,

        @field:NotNull
        @field:JsonDeserialize(using = BooleansDeserializer::class)
        @get:JsonProperty("requiresElectronicCatalogue")
        val requiresElectronicCatalogue: Boolean?,

        @field:NotEmpty
        val submissionMethod: List<SubmissionMethod>?,

        val submissionMethodRationale: List<String>?,

        val submissionMethodDetails: String?,

        @field:Valid
        var documents: List<Document>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class LotCn @JsonCreator constructor(

        @field:NotNull
        var id: String?,

        @field:NotNull
        val title: String?,

        @field:NotNull
        val description: String?,

        var status: TenderStatus?,

        var statusDetails: TenderStatusDetails?,

        @field:Valid @field:NotNull
        val value: Value?,

        @field:Valid @field:NotEmpty
        val options: List<Option>?,

        @field:Valid @field:NotEmpty
        val variants: List<Variant>?,

        @field:Valid @field:NotEmpty
        val renewals: List<Renewal>?,

        @field:Valid @field:NotEmpty
        val recurrentProcurement: List<RecurrentProcurement>?,

        @field:Valid @field:NotNull
        val contractPeriod: Period?,

        @field:Valid @field:NotNull
        val placeOfPerformance: PlaceOfPerformance?
)