package com.procurement.access.application.service.fe.update

import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.MainGeneralActivity
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.MainSectoralActivity
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.domain.model.enums.RelatedProcessScheme
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.enums.TypeOfBuyer
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.model.process.RelatedProcessId
import com.procurement.access.domain.model.process.RelatedProcessIdentifier
import com.procurement.access.domain.model.requirement.Requirement
import java.time.LocalDateTime

data class AmendFEResult(
    val tender: Tender,
    val parties: List<Party>,
    val relatedProcesses: List<RelatedProcess>
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
        val procurementMethodModalities: List<ProcurementMethodModalities>?,
        val procurementMethodRationale: String?,
        val procuringEntity: ProcuringEntity,
        val criteria: List<Criteria>?,
        val otherCriteria: OtherCriteria,
        val documents: List<Document>?
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
            val relatesTo: CriteriaRelatesTo,
            val source: CriteriaSource,
            val description: String?,
            val classification: Classification?,
            val requirementGroups: List<RequirementGroup>
        ) {
            data class RequirementGroup(
                val id: String,
                val description: String?,
                val requirements: List<Requirement>
            )

            data class Classification(
                val scheme: String,
                val id: CPVCode
            )
        }

        data class SecondStage(
            val minimumCandidates: Int?,
            val maximumCandidates: Int?
        )

        data class ProcuringEntity(
            val id: String,
            val name: String
        )

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

    data class Party(
        val id: String,
        val name: String,
        val identifier: Identifier,

        val additionalIdentifiers: List<AdditionalIdentifier>?,

        val address: Address,
        val contactPoint: ContactPoint,
        val roles: List<PartyRole>,
        val persones: List<Person>?,
        val details: Details?
    ) {
        data class Identifier(
            val scheme: String,
            val id: String,
            val legalName: String,
            val uri: String?
        )

        data class AdditionalIdentifier(
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
            val id: PersonId,
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
                val documents: List<Document>?
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

        data class Details(
            val typeOfBuyer: TypeOfBuyer?,
            val mainGeneralActivity: MainGeneralActivity?,
            val mainSectoralActivity: MainSectoralActivity?
        )
    }

    data class RelatedProcess(
        val id: RelatedProcessId,
        val relationship: List<RelatedProcessType>,
        val scheme: RelatedProcessScheme,
        val identifier: RelatedProcessIdentifier,
        val uri: String
    )
}