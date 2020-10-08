package com.procurement.access.application.service.ap.create

import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import java.time.LocalDateTime

data class ApCreateResult(
    val ocid: String,
    val token: String,
    val tender: Tender
) {
    data class Tender(
        val id: String,
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails,
        val title: String,
        val description: String,
        val classification: Classification,
        val acceleratedProcedure: AcceleratedProcedure,
        val designContest: DesignContest,
        val electronicWorkflows: ElectronicWorkflows,
        val jointProcurement: JointProcurement,
        val procedureOutsourcing: ProcedureOutsourcing,
        val framework: Framework,
        val dynamicPurchasingSystem: DynamicPurchasingSystem,
        val legalBasis: LegalBasis?,
        val procurementMethod: ProcurementMethod,
        val procurementMethodDetails: String,

        val procurementMethodRationale: String?,
        val eligibilityCriteria: String,
        val tenderPeriod: TenderPeriod,
        val contractPeriod: ContractPeriod,
        val procuringEntity: ProcuringEntity,
        val requiresElectronicCatalogue: Boolean,
        val submissionMethod: List<SubmissionMethod>,
        val submissionMethodRationale: List<String>,
        val submissionMethodDetails: String,
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

        data class TenderPeriod(
            val startDate: LocalDateTime
        )


        data class ContractPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class ProcuringEntity(
            val id: String,
            val name: String,
            val identifier: Identifier,
            val additionalIdentifiers: List<AdditionalIdentifier>,
            val address: Address,
            val contactPoint: ContactPoint
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
        }

        data class Document(
            val id: String,
            val documentType: DocumentType,
            val title: String,
            val description: String?
        )
    }
}
