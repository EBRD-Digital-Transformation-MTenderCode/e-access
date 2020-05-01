package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.cn.update.UpdateCnData
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.UpdateCnRequest
import com.procurement.access.lib.errorIfEmpty
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow
import com.procurement.access.lib.takeIfNotEmpty

fun UpdateCnRequest.convert() = UpdateCnData(
    planning = this.planning?.let { planning ->
        UpdateCnData.Planning(
            rationale = planning.rationale,
            budget = planning.budget?.let { budget ->
                UpdateCnData.Planning.Budget(
                    description = budget.description
                )
            }
        )
    },
    tender = this.tender.let { tender ->
        UpdateCnData.Tender(
            title = tender.title.takeIfNotEmpty {
                ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "The attribute 'tender.title' is empty or blank."
                )
            },
            description = tender.description.takeIfNotEmpty {
                throw ErrorException(
                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                    message = "The attribute 'tender.description' is empty or blank."
                )
            },
            procurementMethodRationale = tender.procurementMethodRationale,
            procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
            tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                UpdateCnData.Tender.TenderPeriod(
                    startDate = tenderPeriod.startDate,
                    endDate = tenderPeriod.endDate
                )
            },
            enquiryPeriod = tender.enquiryPeriod.let { enquiryPeriod ->
                UpdateCnData.Tender.EnquiryPeriod(
                    startDate = enquiryPeriod.startDate,
                    endDate = enquiryPeriod.endDate
                )
            },
            procurementMethodModalities = tender.procurementMethodModalities
                .errorIfEmpty {
                    ErrorException(
                        error = ErrorType.IS_EMPTY,
                        message = "The tender contain empty list of the procurement method modalities."
                    )
                }
                ?.toList()
                .orEmpty(),
            electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                UpdateCnData.Tender.ElectronicAuctions(
                    details = electronicAuctions.details
                        .mapIfNotEmpty { detail ->
                            UpdateCnData.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = LotId.fromString(detail.relatedLot),
                                electronicAuctionModalities = detail.electronicAuctionModalities
                                    .mapIfNotEmpty { modality ->
                                        UpdateCnData.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                            eligibleMinimumDifference = modality.eligibleMinimumDifference
                                        )
                                    }
                                    .orThrow {
                                        ErrorException(
                                            error = ErrorType.IS_EMPTY,
                                            message = "The electronic auction with id: '${detail.id}' contain empty list of the electronic auction modalities."
                                        )
                                    }
                            )
                        }
                        .orThrow {
                            ErrorException(
                                error = ErrorType.IS_EMPTY,
                                message = "The electronic auctions contain empty list of the details."
                            )
                        }
                )
            },
            procuringEntity = tender.procuringEntity?.let { procuringEntity ->
                UpdateCnData.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    persons = procuringEntity.persons
                        .errorIfEmpty {
                            ErrorException(
                                error = ErrorType.IS_EMPTY,
                                message = "The tender contain empty list of persons in procuringEntity."
                            )
                        }
                        ?.map { person ->
                            UpdateCnData.Tender.ProcuringEntity.Person(
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier.let { identifier ->
                                    UpdateCnData.Tender.ProcuringEntity.Person.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                                businessFunctions = person.businessFunctions
                                    .mapIfNotEmpty { businessFunction ->
                                        UpdateCnData.Tender.ProcuringEntity.Person.BusinessFunction(
                                            id = businessFunction.id,
                                            type = businessFunction.type,
                                            jobTitle = businessFunction.jobTitle,
                                            period = businessFunction.period.let { period ->
                                                UpdateCnData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                    startDate = period.startDate
                                                )
                                            },
                                            documents = businessFunction.documents
                                                .errorIfEmpty {
                                                    ErrorException(
                                                        error = ErrorType.IS_EMPTY,
                                                        message = "The business function with id: '${businessFunction.id}' contain empty list of the documents."
                                                    )
                                                }
                                                ?.map { document ->
                                                    UpdateCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                                                        id = document.id,
                                                        documentType = document.documentType,
                                                        title = document.title,
                                                        description = document.description
                                                    )
                                                }
                                                .orEmpty()
                                        )
                                    }
                                    .orThrow {
                                        ErrorException(
                                            error = ErrorType.IS_EMPTY,
                                            message = "The person contain empty list of the business functions."
                                        )
                                    }
                            )
                        }
                        .orEmpty()
                )
            },
            lots = tender.lots
                .mapIfNotEmpty { lot ->
                    UpdateCnData.Tender.Lot(
                        id = LotId.fromString(lot.id),
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        value = lot.value,
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdateCnData.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdateCnData.Tender.Lot.PlaceOfPerformance(
                                address = placeOfPerformance.address.let { address ->
                                    UpdateCnData.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdateCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdateCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdateCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdateCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                }
                .orThrow {
                    ErrorException(
                        error = ErrorType.IS_EMPTY,
                        message = "The tender contain empty list of the lots."
                    )
                },
            items = tender.items
                .mapIfNotEmpty { item ->
                    UpdateCnData.Tender.Item(
                        id = item.id,
                        internalId = item.internalId,
                        description = item.description,
                        relatedLot = LotId.fromString(item.relatedLot)
                    )
                }
                .orThrow {
                    ErrorException(
                        error = ErrorType.IS_EMPTY,
                        message = "The tender contain empty list of the items."
                    )
                },
            documents = tender.documents
                .mapIfNotEmpty { document ->
                    UpdateCnData.Tender.Document(
                        documentType = document.documentType,
                        id = document.id,
                        title = document.title,
                        description = document.description,
                        relatedLots = document.relatedLots?.map { LotId.fromString(it) }?.toList().orEmpty()
                    )
                }
                .orThrow {
                    ErrorException(
                        error = ErrorType.IS_EMPTY,
                        message = "The tender contain empty list of the documents."
                    )
                }
        )
    }
)
