package com.procurement.access.infrastructure.dto.fe.update

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.bind.criteria.RequirementDeserializer
import com.procurement.access.infrastructure.bind.criteria.RequirementSerializer
import com.procurement.access.infrastructure.bind.money.MoneyDeserializer
import com.procurement.access.infrastructure.bind.money.MoneySerializer
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.model.dto.databinding.JsonDateTimeDeserializer
import com.procurement.access.model.dto.databinding.JsonDateTimeSerializer
import java.time.LocalDateTime

data class AmendFEResponse(
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender
) {
    data class Tender(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails,

        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,

        @JsonDeserialize(using = MoneyDeserializer::class)
        @JsonSerialize(using = MoneySerializer::class)
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Money,

        @field:JsonProperty("acceleratedProcedure") @param:JsonProperty("acceleratedProcedure") val acceleratedProcedure: AcceleratedProcedure,
        @field:JsonProperty("designContest") @param:JsonProperty("designContest") val designContest: DesignContest,
        @field:JsonProperty("electronicWorkflows") @param:JsonProperty("electronicWorkflows") val electronicWorkflows: ElectronicWorkflows,
        @field:JsonProperty("jointProcurement") @param:JsonProperty("jointProcurement") val jointProcurement: JointProcurement,
        @field:JsonProperty("submissionMethod") @param:JsonProperty("submissionMethod") val submissionMethod: List<SubmissionMethod>,
        @field:JsonProperty("submissionMethodRationale") @param:JsonProperty("submissionMethodRationale") val submissionMethodRationale: List<String>,
        @field:JsonProperty("submissionMethodDetails") @param:JsonProperty("submissionMethodDetails") val submissionMethodDetails: String,
        @field:JsonProperty("procedureOutsourcing") @param:JsonProperty("procedureOutsourcing") val procedureOutsourcing: ProcedureOutsourcing,
        @field:JsonProperty("framework") @param:JsonProperty("framework") val framework: Framework,
        @field:JsonProperty("dynamicPurchasingSystem") @param:JsonProperty("dynamicPurchasingSystem") val dynamicPurchasingSystem: DynamicPurchasingSystem,
        @field:JsonProperty("requiresElectronicCatalogue") @param:JsonProperty("requiresElectronicCatalogue") val requiresElectronicCatalogue: Boolean,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("legalBasis") @param:JsonProperty("legalBasis") val legalBasis: LegalBasis?,

        @field:JsonProperty("procurementMethod") @param:JsonProperty("procurementMethod") val procurementMethod: ProcurementMethod,
        @field:JsonProperty("procurementMethodDetails") @param:JsonProperty("procurementMethodDetails") val procurementMethodDetails: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("mainProcurementCategory") @param:JsonProperty("mainProcurementCategory") val mainProcurementCategory: MainProcurementCategory?,

        @field:JsonProperty("eligibilityCriteria") @param:JsonProperty("eligibilityCriteria") val eligibilityCriteria: String,
        @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("secondStage") @param:JsonProperty("secondStage") val secondStage: SecondStage?,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("procurementMethodModalities") @param:JsonProperty("procurementMethodModalities") val procurementMethodModalities: List<ProcurementMethodModalities>?,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodRationale") @param:JsonProperty("procurementMethodRationale") val procurementMethodRationale: String?,

        @field:JsonProperty("procuringEntity") @param:JsonProperty("procuringEntity") val procuringEntity: ProcuringEntity,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("criteria") @param:JsonProperty("criteria") val criteria: List<Criteria>?,

        @field:JsonProperty("otherCriteria") @param:JsonProperty("otherCriteria") val otherCriteria: OtherCriteria,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?

    ) {

        data class Classification(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String
        )

        data class AcceleratedProcedure(
            @get:JsonProperty("isAcceleratedProcedure") @param:JsonProperty("isAcceleratedProcedure") val isAcceleratedProcedure: Boolean
        )

        data class DesignContest(
            @field:JsonProperty("serviceContractAward") @param:JsonProperty("serviceContractAward") val serviceContractAward: Boolean
        )

        data class ElectronicWorkflows(
            @field:JsonProperty("useOrdering") @param:JsonProperty("useOrdering") val useOrdering: Boolean,
            @field:JsonProperty("usePayment") @param:JsonProperty("usePayment") val usePayment: Boolean,
            @field:JsonProperty("acceptInvoicing") @param:JsonProperty("acceptInvoicing") val acceptInvoicing: Boolean
        )

        data class JointProcurement(
            @get:JsonProperty("isJointProcurement") @param:JsonProperty("isJointProcurement") val isJointProcurement: Boolean
        )

        data class ProcedureOutsourcing(
            @field:JsonProperty("procedureOutsourced") @param:JsonProperty("procedureOutsourced") val procedureOutsourced: Boolean
        )

        data class Framework(
            @get:JsonProperty("isAFramework") @param:JsonProperty("isAFramework") val isAFramework: Boolean
        )

        data class DynamicPurchasingSystem(
            @field:JsonProperty("hasDynamicPurchasingSystem") @param:JsonProperty("hasDynamicPurchasingSystem") val hasDynamicPurchasingSystem: Boolean
        )

        data class ContractPeriod(
            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @JsonDeserialize(using = JsonDateTimeDeserializer::class)
            @JsonSerialize(using = JsonDateTimeSerializer::class)
            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )

        data class Criteria(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
            @field:JsonProperty("relatesTo") @param:JsonProperty("relatesTo") val relatesTo: CriteriaRelatesToEnum,
            @field:JsonProperty("source") @param:JsonProperty("source") val source: CriteriaSource,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

            @field:JsonProperty("requirementGroups") @param:JsonProperty("requirementGroups") val requirementGroups: List<RequirementGroup>
        ) {
            data class RequirementGroup(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("description") @param:JsonProperty("description") val description: String?,

                @JsonDeserialize(using = RequirementDeserializer::class)
                @JsonSerialize(using = RequirementSerializer::class)
                @field:JsonProperty("requirements") @param:JsonProperty("requirements") val requirements: List<Requirement>
            )
        }

        data class SecondStage(
            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("minimumCandidates") @param:JsonProperty("minimumCandidates") val minimumCandidates: Int?,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("maximumCandidates") @param:JsonProperty("maximumCandidates") val maximumCandidates: Int?
        )

        data class ProcuringEntity(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("persones") @param:JsonProperty("persones") val persons: List<Person>,
            @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
            @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,

            @JsonInclude(JsonInclude.Include.NON_EMPTY)
            @field:JsonProperty("additionalIdentifiers") @param:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<Identifier>?,
            @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,
            @field:JsonProperty("contactPoint") @param:JsonProperty("contactPoint") val contactPoint: ContactPoint
        ) {
            data class Identifier(
                @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
            )

            data class Address(
                @field:JsonProperty("streetAddress") @param:JsonProperty("streetAddress") val streetAddress: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("postalCode") @param:JsonProperty("postalCode") val postalCode: String?,
                @field:JsonProperty("addressDetails") @param:JsonProperty("addressDetails") val addressDetails: AddressDetails
            ) {

                data class AddressDetails(
                    @field:JsonProperty("country") @param:JsonProperty("country") val country: Country,
                    @field:JsonProperty("region") @param:JsonProperty("region") val region: Region,
                    @field:JsonProperty("locality") @param:JsonProperty("locality") val locality: Locality
                ) {

                    data class Country(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                    )

                    data class Region(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String
                    )

                    data class Locality(
                        @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,

                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                    )
                }
            }

            data class ContactPoint(
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                @field:JsonProperty("email") @param:JsonProperty("email") val email: String,
                @field:JsonProperty("telephone") @param:JsonProperty("telephone") val telephone: String,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("faxNumber") @param:JsonProperty("faxNumber") val faxNumber: String?,

                @JsonInclude(JsonInclude.Include.NON_NULL)
                @field:JsonProperty("url") @param:JsonProperty("url") val url: String?
            )

            data class Person(
                @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
                @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
                @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,
                @field:JsonProperty("businessFunctions") @param:JsonProperty("businessFunctions") val businessFunctions: List<BusinessFunction>
            ) {
                data class Identifier(
                    @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,

                    @JsonInclude(JsonInclude.Include.NON_NULL)
                    @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
                )

                data class BusinessFunction(
                    @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                    @field:JsonProperty("type") @param:JsonProperty("type") val type: BusinessFunctionType,
                    @field:JsonProperty("jobTitle") @param:JsonProperty("jobTitle") val jobTitle: String,
                    @field:JsonProperty("period") @param:JsonProperty("period") val period: Period,

                    @JsonInclude(JsonInclude.Include.NON_EMPTY)
                    @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document>?
                ) {
                    data class Document(
                        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
                        @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: BusinessFunctionDocumentType,
                        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

                        @JsonInclude(JsonInclude.Include.NON_NULL)
                        @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
                    )

                    data class Period(
                        @JsonDeserialize(using = JsonDateTimeDeserializer::class)
                        @JsonSerialize(using = JsonDateTimeSerializer::class)
                        @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
                    )
                }
            }
        }

        data class OtherCriteria(
            @field:JsonProperty("reductionCriteria") @param:JsonProperty("reductionCriteria") val reductionCriteria: ReductionCriteria,
            @field:JsonProperty("qualificationSystemMethods") @param:JsonProperty("qualificationSystemMethods") val qualificationSystemMethods: List<QualificationSystemMethod>
        )

        data class Document(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: DocumentType,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
        )
    }
}