package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.cn.update.UpdateOpenCnData
import com.procurement.access.application.service.cn.update.UpdateSelectiveCnData
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.UpdateOpenCnRequest
import com.procurement.access.infrastructure.dto.cn.UpdateSelectiveCnRequest
import com.procurement.access.lib.errorIfEmpty
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow
import com.procurement.access.lib.takeIfNotEmpty

fun UpdateOpenCnRequest.convert() = UpdateOpenCnData(
    planning = this.planning?.let { planning ->
        UpdateOpenCnData.Planning(
            rationale = planning.rationale,
            budget = planning.budget?.let { budget ->
                UpdateOpenCnData.Planning.Budget(
                    description = budget.description
                )
            }
        )
    },
    tender = this.tender.let { tender ->
        UpdateOpenCnData.Tender(
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
                UpdateOpenCnData.Tender.TenderPeriod(
                    startDate = tenderPeriod.startDate,
                    endDate = tenderPeriod.endDate
                )
            },
            enquiryPeriod = tender.enquiryPeriod
                ?.let { enquiryPeriod ->
                    UpdateOpenCnData.Tender.EnquiryPeriod(
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
                UpdateOpenCnData.Tender.ElectronicAuctions(
                    details = electronicAuctions.details
                        .mapIfNotEmpty { detail ->
                            UpdateOpenCnData.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = LotId.fromString(detail.relatedLot),
                                electronicAuctionModalities = detail.electronicAuctionModalities
                                    .mapIfNotEmpty { modality ->
                                        UpdateOpenCnData.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
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
                UpdateOpenCnData.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    persons = procuringEntity.persons
                        .errorIfEmpty {
                            ErrorException(
                                error = ErrorType.IS_EMPTY,
                                message = "The tender contain empty list of persons in procuringEntity."
                            )
                        }
                        ?.map { person ->
                            UpdateOpenCnData.Tender.ProcuringEntity.Person(
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier.let { identifier ->
                                    UpdateOpenCnData.Tender.ProcuringEntity.Person.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                                businessFunctions = person.businessFunctions
                                    .mapIfNotEmpty { businessFunction ->
                                        UpdateOpenCnData.Tender.ProcuringEntity.Person.BusinessFunction(
                                            id = businessFunction.id,
                                            type = businessFunction.type,
                                            jobTitle = businessFunction.jobTitle,
                                            period = businessFunction.period.let { period ->
                                                UpdateOpenCnData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
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
                                                    UpdateOpenCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
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
                    UpdateOpenCnData.Tender.Lot(
                        id = LotId.fromString(lot.id),
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        value = lot.value,
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdateOpenCnData.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdateOpenCnData.Tender.Lot.PlaceOfPerformance(
                                address = placeOfPerformance.address.let { address ->
                                    UpdateOpenCnData.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdateOpenCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdateOpenCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdateOpenCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdateOpenCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    UpdateOpenCnData.Tender.Item(
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
                    UpdateOpenCnData.Tender.Document(
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

fun UpdateSelectiveCnRequest.convert() = UpdateSelectiveCnData(
    planning = this.planning?.let { planning ->
        UpdateSelectiveCnData.Planning(
            rationale = planning.rationale,
            budget = planning.budget?.let { budget ->
                UpdateSelectiveCnData.Planning.Budget(
                    description = budget.description
                )
            }
        )
    },
    tender = this.tender.let { tender ->
        UpdateSelectiveCnData.Tender(
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
                UpdateSelectiveCnData.Tender.ElectronicAuctions(
                    details = electronicAuctions.details
                        .mapIfNotEmpty { detail ->
                            UpdateSelectiveCnData.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = LotId.fromString(detail.relatedLot),
                                electronicAuctionModalities = detail.electronicAuctionModalities
                                    .mapIfNotEmpty { modality ->
                                        UpdateSelectiveCnData.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
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
                UpdateSelectiveCnData.Tender.ProcuringEntity(
                    id = procuringEntity.id,
                    persons = procuringEntity.persons
                        .errorIfEmpty {
                            ErrorException(
                                error = ErrorType.IS_EMPTY,
                                message = "The tender contain empty list of persons in procuringEntity."
                            )
                        }
                        ?.map { person ->
                            UpdateSelectiveCnData.Tender.ProcuringEntity.Person(
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier.let { identifier ->
                                    UpdateSelectiveCnData.Tender.ProcuringEntity.Person.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                                businessFunctions = person.businessFunctions
                                    .mapIfNotEmpty { businessFunction ->
                                        UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction(
                                            id = businessFunction.id,
                                            type = businessFunction.type,
                                            jobTitle = businessFunction.jobTitle,
                                            period = businessFunction.period.let { period ->
                                                UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
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
                                                    UpdateSelectiveCnData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
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
                    UpdateSelectiveCnData.Tender.Lot(
                        id = LotId.fromString(lot.id),
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        value = lot.value,
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdateSelectiveCnData.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance(
                                address = placeOfPerformance.address.let { address ->
                                    UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdateSelectiveCnData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    UpdateSelectiveCnData.Tender.Item(
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
                    UpdateSelectiveCnData.Tender.Document(
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
                },
            otherCriteria = tender.otherCriteria
                .let { otherCriteria ->
                    UpdateSelectiveCnData.Tender.OtherCriteria(
                        reductionCriteria = otherCriteria.reductionCriteria,
                        qualificationSystemMethods = otherCriteria.qualificationSystemMethods.toList()
                    )
                }
        )
    }
)