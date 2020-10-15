package com.procurement.access.application.service.ap.create

import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.Scheme
import java.time.LocalDateTime

class ApCreateData(
    val tender: Tender
) {
    data class Tender(
        val title: String,
        val description: String,
        val classification: Classification,
        val value: Value,
        val legalBasis: LegalBasis?,
        val procurementMethodDetails: String,
        val procurementMethodRationale: String?,
        val eligibilityCriteria: String,
        val tenderPeriod: TenderPeriod,
        val contractPeriod: ContractPeriod,
        val procuringEntity: ProcuringEntity,
        val submissionMethodRationale: List<String>,
        val submissionMethodDetails: String,
        val documents: List<Document>
    ) {
        data class Classification(
            val scheme: Scheme,
            val id: CPVCode,
            val description: String
        )

        data class Value(
            val currency: String
        )

        data class TenderPeriod(
            val startDate: LocalDateTime
        )

        data class ContractPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class ProcuringEntity(
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
