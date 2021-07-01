package com.procurement.access.infrastructure.handler.v1.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.procurement.access.domain.model.CPVCode
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import java.time.LocalDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class ApCreateResponse(
    @field:JsonProperty("cpid") @param:JsonProperty("cpid") val cpid: String,
    @field:JsonProperty("ocid") @param:JsonProperty("ocid") val ocid: String,
    @field:JsonProperty("token") @param:JsonProperty("token") val token: String,
    @field:JsonProperty("tender") @param:JsonProperty("tender") val tender: Tender,
    @field:JsonProperty("parties") @param:JsonProperty("parties") val parties: List<Party>
) {
    data class Tender(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("status") @param:JsonProperty("status") val status: TenderStatus,
        @field:JsonProperty("statusDetails") @param:JsonProperty("statusDetails") val statusDetails: TenderStatusDetails,
        @field:JsonProperty("title") @param:JsonProperty("title") val title: String,
        @field:JsonProperty("description") @param:JsonProperty("description") val description: String,
        @field:JsonProperty("classification") @param:JsonProperty("classification") val classification: Classification,
        @field:JsonProperty("value") @param:JsonProperty("value") val value: Value,

        @field:JsonProperty("acceleratedProcedure") @param:JsonProperty("acceleratedProcedure") val acceleratedProcedure: AcceleratedProcedure,
        @field:JsonProperty("designContest") @param:JsonProperty("designContest") val designContest: DesignContest,
        @field:JsonProperty("electronicWorkflows") @param:JsonProperty("electronicWorkflows") val electronicWorkflows: ElectronicWorkflows,
        @field:JsonProperty("jointProcurement") @param:JsonProperty("jointProcurement") val jointProcurement: JointProcurement,
        @field:JsonProperty("procedureOutsourcing") @param:JsonProperty("procedureOutsourcing") val procedureOutsourcing: ProcedureOutsourcing,
        @field:JsonProperty("framework") @param:JsonProperty("framework") val framework: Framework,
        @field:JsonProperty("dynamicPurchasingSystem") @param:JsonProperty("dynamicPurchasingSystem") val dynamicPurchasingSystem: DynamicPurchasingSystem,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("legalBasis") @param:JsonProperty("legalBasis") val legalBasis: LegalBasis?,

        @field:JsonProperty("procurementMethod") @param:JsonProperty("procurementMethod") val procurementMethod: ProcurementMethod,
        @field:JsonProperty("procurementMethodDetails") @param:JsonProperty("procurementMethodDetails") val procurementMethodDetails: String,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("procurementMethodRationale") @param:JsonProperty("procurementMethodRationale") val procurementMethodRationale: String?,

        @field:JsonProperty("eligibilityCriteria") @param:JsonProperty("eligibilityCriteria") val eligibilityCriteria: String,
        @field:JsonProperty("tenderPeriod") @param:JsonProperty("tenderPeriod") val tenderPeriod: TenderPeriod,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @field:JsonProperty("contractPeriod") @param:JsonProperty("contractPeriod") val contractPeriod: ContractPeriod,

        @field:JsonProperty("requiresElectronicCatalogue") @param:JsonProperty("requiresElectronicCatalogue") val requiresElectronicCatalogue: Boolean,
        @field:JsonProperty("submissionMethod") @param:JsonProperty("submissionMethod") val submissionMethod: List<SubmissionMethod>,
        @field:JsonProperty("submissionMethodRationale") @param:JsonProperty("submissionMethodRationale") val submissionMethodRationale: List<String>,
        @field:JsonProperty("submissionMethodDetails") @param:JsonProperty("submissionMethodDetails") val submissionMethodDetails: String,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("documents") @param:JsonProperty("documents") val documents: List<Document> = emptyList()
    ) {

        data class Classification(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: Scheme,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: CPVCode,
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String
        )

        data class Value(
            @field:JsonProperty("currency") @param:JsonProperty("currency") val currency: String
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

        data class TenderPeriod(
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime
        )

        data class ContractPeriod(
            @field:JsonProperty("startDate") @param:JsonProperty("startDate") val startDate: LocalDateTime,

            @field:JsonProperty("endDate") @param:JsonProperty("endDate") val endDate: LocalDateTime
        )

        data class Document(
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("documentType") @param:JsonProperty("documentType") val documentType: DocumentType,
            @field:JsonProperty("title") @param:JsonProperty("title") val title: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("description") @param:JsonProperty("description") val description: String?
        )
    }

    data class Party(
        @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
        @field:JsonProperty("name") @param:JsonProperty("name") val name: String,
        @field:JsonProperty("identifier") @param:JsonProperty("identifier") val identifier: Identifier,

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @field:JsonProperty("additionalIdentifiers") @param:JsonProperty("additionalIdentifiers") val additionalIdentifiers: List<AdditionalIdentifier> = emptyList(),

        @field:JsonProperty("address") @param:JsonProperty("address") val address: Address,
        @field:JsonProperty("contactPoint") @param:JsonProperty("contactPoint") val contactPoint: ContactPoint,
        @field:JsonProperty("roles") @param:JsonProperty("roles") val roles: List<PartyRole>
    ) {

        data class Identifier(
            @field:JsonProperty("scheme") @param:JsonProperty("scheme") val scheme: String,
            @field:JsonProperty("id") @param:JsonProperty("id") val id: String,
            @field:JsonProperty("legalName") @param:JsonProperty("legalName") val legalName: String,

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @field:JsonProperty("uri") @param:JsonProperty("uri") val uri: String?
        )

        data class AdditionalIdentifier(
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
    }
}