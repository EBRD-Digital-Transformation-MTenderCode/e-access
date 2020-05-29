package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.LegalBasis
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.model.lot.RelatedLot
import com.procurement.access.domain.model.lot.RelatedLots
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.model.persone.PersonId
import java.math.BigDecimal
import java.time.LocalDateTime

data class UpdatedCn(
    val lotsChanged: Boolean,
    val planning: Planning,
    val tender: Tender,
    val amendment: Amendment?
) {

    data class Planning(
        val rationale: String?,
        val budget: Budget
    ) {

        data class Budget(
            val description: String?,
            val amount: Money,
            val isEuropeanUnionFunded: Boolean,
            val budgetBreakdowns: List<BudgetBreakdown>
        ) {

            data class BudgetBreakdown(
                val id: String,
                val description: String?,
                val amount: Money,
                val period: Period,
                val sourceParty: SourceParty,
                val europeanUnionFunding: EuropeanUnionFunding?
            ) {
                data class Period(
                    val startDate: LocalDateTime,
                    val endDate: LocalDateTime
                )

                data class SourceParty(
                    val id: String,
                    val name: String
                )

                data class EuropeanUnionFunding(
                    val projectIdentifier: String,
                    val projectName: String,
                    val uri: String?
                )
            }
        }
    }

    data class Tender(
        val id: String,
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails,
        val title: String,
        val description: String,
        val classification: Classification,
        val tenderPeriod: TenderPeriod,
        val enquiryPeriod: EnquiryPeriod?,
        val acceleratedProcedure: AcceleratedProcedure,
        val designContest: DesignContest,
        val electronicWorkflows: ElectronicWorkflows,
        val jointProcurement: JointProcurement,
        val procedureOutsourcing: ProcedureOutsourcing,
        val framework: Framework,
        val dynamicPurchasingSystem: DynamicPurchasingSystem,
        val legalBasis: LegalBasis,
        val procurementMethod: ProcurementMethod,
        val procurementMethodDetails: String,
        val procurementMethodRationale: String?,
        val procurementMethodAdditionalInfo: String?,
        val mainProcurementCategory: MainProcurementCategory,
        val eligibilityCriteria: String,
        val contractPeriod: ContractPeriod,
        val procurementMethodModalities: List<ProcurementMethodModalities>,
        val electronicAuctions: ElectronicAuctions?,
        val procuringEntity: ProcuringEntity,
        val value: Money,
        val lotGroups: List<LotGroup>,
        val lots: List<Lot>,
        val items: List<Item>,
        val requiresElectronicCatalogue: Boolean,
        val submissionMethod: List<SubmissionMethod>,
        val submissionMethodRationale: List<String>,
        val submissionMethodDetails: String,
        val documents: List<Document>,
        val otherCriteria: OtherCriteria?
    ) {

        data class Classification(
            val scheme: Scheme,
            val id: String,
            val description: String
        )

        data class TenderPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class EnquiryPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
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

        data class ElectronicAuctions(
            val details: List<Detail>
        ) {

            data class Detail(
                val id: String,
                override val relatedLot: LotId,
                val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) : RelatedLot<LotId> {

                data class ElectronicAuctionModality(
                    val eligibleMinimumDifference: Money
                )
            }
        }

        data class ProcuringEntity(
            val id: String,
            val name: String,
            val identifier: Identifier,
            val additionalIdentifiers: List<AdditionalIdentifier>,
            val address: Address,
            val persons: List<Person>,
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

            data class Person(
                val id: PersonId,
                val title: String,
                val name: String,
                val identifier: Identifier,
                val businessFunctions: List<BusinessFunction>
            ) {

                data class Identifier(
                    val id: String,
                    val scheme: String,
                    val uri: String?
                )

                data class BusinessFunction(
                    val id: String,
                    val type: BusinessFunctionType,
                    val jobTitle: String,
                    val period: Period,
                    val documents: List<Document>
                ) {

                    data class Period(
                        val startDate: LocalDateTime
                    )

                    data class Document(
                        val documentType: BusinessFunctionDocumentType,
                        val id: String,
                        val title: String,
                        val description: String?
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

        data class LotGroup(
            val optionToCombine: Boolean
        )

        data class Lot(
            val id: LotId,
            val internalId: String?,
            val title: String,
            val description: String,
            val status: LotStatus,
            val statusDetails: LotStatusDetails,
            val value: Money,
            val options: List<Option>,
            val variants: List<Variant>,
            val renewals: List<Renewal>,
            val recurrentProcurements: List<RecurrentProcurement>,
            val contractPeriod: ContractPeriod,
            val placeOfPerformance: PlaceOfPerformance
        ) {

            data class Option(
                val hasOptions: Boolean
            )

            data class Variant(
                val hasVariants: Boolean
            )

            data class Renewal(
                val hasRenewals: Boolean
            )

            data class RecurrentProcurement(
                val isRecurrent: Boolean
            )

            data class ContractPeriod(
                val startDate: LocalDateTime,
                val endDate: LocalDateTime
            )

            data class PlaceOfPerformance(
                val address: Address,
                val description: String?
            ) {

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
            }
        }

        data class Item(
            val id: String,
            val internalId: String?,
            val classification: Classification,
            val additionalClassifications: List<AdditionalClassification>,
            val quantity: BigDecimal,
            val unit: Unit,
            val description: String,
            override val relatedLot: LotId
        ) : RelatedLot<LotId> {

            data class Classification(
                val scheme: Scheme,
                val id: String,
                val description: String
            )

            data class AdditionalClassification(
                val scheme: Scheme,
                val id: String,
                val description: String
            )

            data class Unit(
                val id: String,
                val name: String
            )
        }

        data class Document(
            val documentType: DocumentType,
            val id: String,
            val title: String?,
            val description: String?,
            override val relatedLots: List<LotId>
        ) : RelatedLots<LotId>

        data class OtherCriteria(
            val reductionCriteria: ReductionCriteria,
            val qualificationSystemMethods: List<QualificationSystemMethod>
        )
    }

    data class Amendment(
        override val relatedLots: List<LotId>
    ) : RelatedLots<LotId>
}
