package com.procurement.access.application.service.fe.create

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
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import java.time.LocalDateTime

data class CreateFEResult(
    val ocid: String,
    val token: String,
    val tender: Tender
) {
    data class Tender(
        val id: String,
        val title: String,
        val description: String,
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails,
        val classification: Classification,
        val value: Money,
        val acceleratedProcedure: AcceleratedProcedure,
        val designContest: DesignContest,
        val electronicWorkflows: ElectronicWorkflows,
        val jointProcurement: JointProcurement,
        val submissionMethod: List<SubmissionMethod>,
        val submissionMethodRationale: List<String>,
        val submissionMethodDetails: String,
        val procedureOutsourcing: ProcedureOutsourcing,
        val framework: Framework,
        val dynamicPurchasingSystem: DynamicPurchasingSystem,
        val requiresElectronicCatalogue: Boolean,
        val legalBasis: LegalBasis?,
        val procurementMethod: ProcurementMethod,
        val procurementMethodDetails: String,
        val mainProcurementCategory: MainProcurementCategory?,
        val eligibilityCriteria: String,
        val contractPeriod: ContractPeriod,
        val secondStage: SecondStage?,
        val procurementMethodModalities: List<ProcurementMethodModalities>,
        val procurementMethodRationale: String?,
        val procuringEntity: ProcuringEntity,
        val criteria: List<Criteria>,
        val otherCriteria: OtherCriteria,
        val documents: List<Document>
    ) {

        data class Classification(
            val scheme: Scheme,
            val id: CPVCode,
            val description: String
        )

        data class AcceleratedProcedure(
            val isAcceleratedProcedure: Boolean
        )

        data class DesignContest(
            val serviceContractAward: Boolean
        )

        data class ElectronicWorkflows(
            val useOrdering: Boolean,
            val usePayment: Boolean,
            val acceptInvoicing: Boolean
        )

        data class JointProcurement(
            val isJointProcurement: Boolean
        )

        data class ProcedureOutsourcing(
            val procedureOutsourced: Boolean
        )

        data class Framework(
            val isAFramework: Boolean
        )

        data class DynamicPurchasingSystem(
            val hasDynamicPurchasingSystem: Boolean
        )

        data class ContractPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class Criteria(
            val id: String,
            val title: String,
            val relatesTo: CriteriaRelatesToEnum,
            val source: CriteriaSource,
            val description: String?,
            val requirementGroups: List<RequirementGroup>
        ) {
            data class RequirementGroup(
                val id: String,
                val description: String?,
                val requirements: List<Requirement>
            )
        }

        data class SecondStage(
            val minimumCandidates: Int?,
            val maximumCandidates: Int?
        )

        data class ProcuringEntity(
            val id: String,
            val persons: List<Person>,
            val name: String,
            val identifier: Identifier,
            val additionalIdentifiers: List<Identifier>,
            val address: Address,
            val contactPoint: ContactPoint
        ) {
            data class Identifier(
                val scheme: String,
                val id: String,
                val legalName: String,
                val uri: String?
            )

            data class Address(
                val streetAddress: String,
                val postalCode: String?,
                val addressDetails: AddressDetails
            ) {

                data class AddressDetails(
                    val country: Country,
                    val region: Region,
                    val locality: Locality
                ) {

                    data class Country(
                        val scheme: String,
                        val id: String,
                        val description: String,
                        val uri: String
                    )

                    data class Region(
                        val scheme: String,
                        val id: String,
                        val description: String,
                        val uri: String
                    )

                    data class Locality(
                        val scheme: String,
                        val id: String,
                        val description: String,
                        val uri: String?
                    )
                }
            }

            data class ContactPoint(
                val name: String,
                val email: String,
                val telephone: String,
                val faxNumber: String?,
                val url: String?
            )

            data class Person(
                val id: String,
                val title: String,
                val name: String,
                val identifier: Identifier,
                val businessFunctions: List<BusinessFunction>
            ) {
                data class Identifier(
                    val scheme: String,
                    val id: String,
                    val uri: String?
                )

                data class BusinessFunction(
                    val id: String,
                    val type: BusinessFunctionType,
                    val jobTitle: String,
                    val period: Period,
                    val documents: List<Document>
                ) {
                    data class Document(
                        val id: String,
                        val documentType: BusinessFunctionDocumentType,
                        val title: String,
                        val description: String?
                    )

                    data class Period(
                        val startDate: LocalDateTime
                    )
                }
            }
        }

        data class OtherCriteria(
            val reductionCriteria: ReductionCriteria,
            val qualificationSystemMethods: List<QualificationSystemMethod>
        )

        data class Document(
            val id: String,
            val documentType: DocumentType,
            val title: String,
            val description: String?
        )
    }
}