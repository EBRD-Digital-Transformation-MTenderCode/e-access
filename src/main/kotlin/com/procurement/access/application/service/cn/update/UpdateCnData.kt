package com.procurement.access.application.service.cn.update

import com.procurement.access.domain.model.EntityBase
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesToEnum
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.TenderDocumentType
import com.procurement.access.domain.model.lot.RelatedLot
import com.procurement.access.domain.model.lot.RelatedLots
import com.procurement.access.domain.model.money.Money
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import java.math.BigDecimal
import java.time.LocalDateTime

data class UpdateCnData(
    val planning: Planning?,
    val tender: Tender
) {
    data class Planning(
        val rationale: String?,
        val budget: Budget?
    ) {

        data class Budget(
            val description: String
        )
    }

    data class Tender(
        val title: String,
        val description: String,
        val procurementMethodRationale: String?,
        val procurementMethodAdditionalInfo: String?,
        val awardCriteria: AwardCriteria,
        val awardCriteriaDetails: AwardCriteriaDetails?,
        val tenderPeriod: TenderPeriod,
        val procurementMethodModalities: List<ProcurementMethodModalities>?,
        val electronicAuctions: ElectronicAuctions?,
        val criteria: List<Criteria>,
        val conversions: List<Conversion>,
        val procuringEntity: ProcuringEntity?,
        val lots: List<Lot>,
        val items: List<Item>,
        val documents: List<Document>
    ) {

        data class TenderPeriod(
            val startDate: LocalDateTime,
            val endDate: LocalDateTime
        )

        data class ElectronicAuctions(
            val details: List<Detail>
        ) {

            data class Detail(
                override val id: String,
                override val relatedLot: String,
                val electronicAuctionModalities: List<ElectronicAuctionModality>
            ) : EntityBase<String>(), RelatedLot<String> {

                data class ElectronicAuctionModality(
                    val eligibleMinimumDifference: Money
                )
            }
        }

        data class Criteria(
            override val id: String,
            val title: String,
            val description: String?,
            val relatesTo: CriteriaRelatesToEnum?,
            val relatedItem: String?,
            val requirementGroups: List<RequirementGroup>

        ) : EntityBase<String>() {

            data class RequirementGroup(
                override val id: String,
                val description: String?,
                val requirements: List<Requirement>
            ) : EntityBase<String>()
        }

        data class Conversion(
            override val id: String,
            val relatesTo: String,
            val relatedItem: String,
            val rationale: String,
            val description: String?,
            val coefficients: List<Coefficient>
        ) : EntityBase<String>() {

            data class Coefficient(
                override val id: String,
                val value: CoefficientValue,
                val coefficient: BigDecimal
            ) : EntityBase<String>()
        }

        data class ProcuringEntity(
            override val id: String,
            val persons: List<Person>
        ) : EntityBase<String>() {

            data class Person(
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
                    override val id: String,
                    val type: BusinessFunctionType,
                    val jobTitle: String,
                    val period: Period,
                    val documents: List<Document>
                ) : EntityBase<String>() {

                    data class Period(
                        val startDate: LocalDateTime
                    )

                    data class Document(
                        override val id: String,
                        val documentType: BusinessFunctionDocumentType,
                        val title: String,
                        val description: String?
                    ) : EntityBase<String>()
                }
            }
        }

        data class Lot(
            override val id: String,
            val internalId: String?,
            val title: String,
            val description: String,
            val value: Money,
            val contractPeriod: ContractPeriod,
            val placeOfPerformance: PlaceOfPerformance
        ) : EntityBase<String>() {

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
            override val id: String,
            val description: String,
            override val relatedLot: String
        ) : EntityBase<String>(), RelatedLot<String>

        data class Document(
            val documentType: TenderDocumentType,
            override val id: String,
            val title: String?,
            val description: String?,
            override val relatedLots: List<String>
        ) : EntityBase<String>(), RelatedLots<String>
    }
}

fun UpdateCnData.replaceTemplateLotIds(r: Map<String, String>) = UpdateCnWithPermanentId(
    planning = this.planning?.let { planning ->
        UpdateCnWithPermanentId.Planning(
            rationale = planning.rationale,
            budget = planning.budget?.let { budget ->
                UpdateCnWithPermanentId.Planning.Budget(
                    description = budget.description
                )
            }
        )
    },
    tender = this.tender.let { tender ->
        UpdateCnWithPermanentId.Tender(
            title = tender.title,
            description = tender.description,
            procurementMethodRationale = tender.procurementMethodRationale,
            procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
            awardCriteria = tender.awardCriteria,
            awardCriteriaDetails = tender.awardCriteriaDetails,
            tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                UpdateCnWithPermanentId.Tender.TenderPeriod(
                    startDate = tenderPeriod.startDate,
                    endDate = tenderPeriod.endDate
                )
            },
            procurementMethodModalities = tender.procurementMethodModalities,
            electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                UpdateCnWithPermanentId.Tender.ElectronicAuctions(
                    details = electronicAuctions.details.map { detail ->
                        UpdateCnWithPermanentId.Tender.ElectronicAuctions.Detail(
                            id = detail.id,
                            relatedLot = r[detail.relatedLot] ?: detail.relatedLot,
                            electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                UpdateCnWithPermanentId.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                    eligibleMinimumDifference = modality.eligibleMinimumDifference
                                )
                            }
                        )
                    }
                )
            },
            criteria = tender.criteria.map { criteria ->
                UpdateCnWithPermanentId.Tender.Criteria(
                    id = criteria.id,
                    title = criteria.title,
                    description = criteria.description,
                    relatesTo = criteria.relatesTo,
                    relatedItem = r[criteria.relatedItem] ?: criteria.relatedItem,
                    requirementGroups = criteria.requirementGroups.map { requirementGroup ->
                        UpdateCnWithPermanentId.Tender.Criteria.RequirementGroup(
                            id = requirementGroup.id,
                            description = requirementGroup.description,
                            requirements = requirementGroup.requirements.toList()
                        )
                    }
                )
            },
            conversions = tender.conversions.map { conversion ->
                UpdateCnWithPermanentId.Tender.Conversion(
                    id = conversion.id,
                    relatesTo = conversion.relatesTo,
                    relatedItem = conversion.relatedItem,
                    rationale = conversion.rationale,
                    description = conversion.description,
                    coefficients = conversion.coefficients.map { coefficient ->
                        UpdateCnWithPermanentId.Tender.Conversion.Coefficient(
                            id = coefficient.id,
                            value = coefficient.value,
                            coefficient = coefficient.coefficient
                        )
                    }
                )
            },
            procuringEntity = tender.procuringEntity?.let { procuringEntity ->
                UpdateCnWithPermanentId.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    persons = procuringEntity.persons.map { person ->
                        UpdateCnWithPermanentId.Tender.ProcuringEntity.Person(
                            title = person.title,
                            name = person.name,
                            identifier = person.identifier.let { identifier ->
                                UpdateCnWithPermanentId.Tender.ProcuringEntity.Person.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    uri = identifier.uri
                                )
                            },
                            businessFunctions = person.businessFunctions.map { businessFunction ->
                                UpdateCnWithPermanentId.Tender.ProcuringEntity.Person.BusinessFunction(
                                    id = businessFunction.id,
                                    type = businessFunction.type,
                                    jobTitle = businessFunction.jobTitle,
                                    period = businessFunction.period.let { period ->
                                        UpdateCnWithPermanentId.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                    documents = businessFunction.documents
                                        .map { document ->
                                            UpdateCnWithPermanentId.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                                                id = document.id,
                                                documentType = document.documentType,
                                                title = document.title,
                                                description = document.description
                                            )
                                        }

                                )
                            }
                        )
                    }
                )
            },
            lots = tender.lots.map { lot ->
                UpdateCnWithPermanentId.Tender.Lot(
                    id = r[lot.id] ?: lot.id,
                    internalId = lot.internalId,
                    title = lot.title,
                    description = lot.description,
                    value = lot.value,
                    contractPeriod = lot.contractPeriod.let { contractPeriod ->
                        UpdateCnWithPermanentId.Tender.Lot.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                    placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                        UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance(
                            address = placeOfPerformance.address.let { address ->
                                UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails.let { addressDetails ->
                                        UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                            country = addressDetails.country.let { country ->
                                                UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                            region = addressDetails.region.let { region ->
                                                UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                            locality = addressDetails.locality.let { locality ->
                                                UpdateCnWithPermanentId.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                    scheme = locality.scheme,
                                                    id = locality.id,
                                                    description = locality.description,
                                                    uri = locality.uri
                                                )
                                            }
                                        )
                                    }
                                )
                            },
                            description = placeOfPerformance.description
                        )
                    }
                )
            },
            items = tender.items.map { item ->
                UpdateCnWithPermanentId.Tender.Item(
                    id = item.id,
                    description = item.description,
                    relatedLot = r[item.relatedLot] ?: item.relatedLot
                )
            },
            documents = tender.documents
                .map { document ->
                    UpdateCnWithPermanentId.Tender.Document(
                        documentType = document.documentType,
                        id = document.id,
                        title = document.title,
                        description = document.description,
                        relatedLots = document.relatedLots.map { relatedLot ->
                            r[relatedLot] ?: relatedLot
                        }
                    )
                }
        )
    }
)