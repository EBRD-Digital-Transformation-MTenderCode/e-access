package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.pn.create.PnCreateData
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.handler.v1.model.request.PnCreateRequest
import com.procurement.access.lib.extension.errorIfEmpty
import com.procurement.access.lib.extension.mapIfNotEmpty
import com.procurement.access.lib.extension.orThrow
import com.procurement.access.lib.takeIfNotEmpty

fun PnCreateRequest.convert() = PnCreateData(
    planning = this.planning
        .let { planning ->
            PnCreateData.Planning(
                rationale = planning.rationale
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'planning.rationale' is empty or blank."
                        )
                    },
                budget = planning.budget
                    .let { budget ->
                        PnCreateData.Planning.Budget(
                            description = budget.description
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'planning.budget.description' is empty or blank."
                                    )
                                },
                            amount = budget.amount,
                            budgetBreakdowns = budget.budgetBreakdowns
                                .mapIfNotEmpty { budgetBreakdowns ->
                                    PnCreateData.Planning.Budget.BudgetBreakdown(
                                        id = budgetBreakdowns.id.takeIfNotEmpty {
                                            ErrorException(
                                                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                message = "The attribute 'budget.id' is empty or blank."
                                            )
                                        },
                                        description = budgetBreakdowns.description,
                                        amount = budgetBreakdowns.amount,
                                        period = budgetBreakdowns.period
                                            .let { period ->
                                                PnCreateData.Planning.Budget.BudgetBreakdown.Period(
                                                    startDate = period.startDate,
                                                    endDate = period.endDate
                                                )
                                            },
                                        sourceParty = budgetBreakdowns.sourceParty
                                            .let { sourceParty ->
                                                PnCreateData.Planning.Budget.BudgetBreakdown.SourceParty(
                                                    name = sourceParty.name,
                                                    id = sourceParty.id
                                                )
                                            },
                                        europeanUnionFunding = budgetBreakdowns.europeanUnionFunding
                                            ?.let { europeanUnionFunding ->
                                                PnCreateData.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                                    projectIdentifier = europeanUnionFunding.projectIdentifier
                                                        .takeIfNotEmpty {
                                                            ErrorException(
                                                                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                message = "The attribute 'europeanUnionFunding.projectIdentifier' is empty or blank."
                                                            )
                                                        },
                                                    projectName = europeanUnionFunding.projectName
                                                        .takeIfNotEmpty {
                                                            ErrorException(
                                                                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                message = "The attribute 'europeanUnionFunding.projectName' is empty or blank."
                                                            )
                                                        },
                                                    uri = europeanUnionFunding.uri
                                                        .takeIfNotEmpty {
                                                            ErrorException(
                                                                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                message = "The attribute 'europeanUnionFunding.uri' is empty or blank."
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                }
                                .orThrow {
                                    ErrorException(
                                        error = ErrorType.IS_EMPTY,
                                        message = "The budget contain empty list of the budgetBreakdowns."
                                    )
                                },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded
                        )
                    }
            )
        },
    tender = this.tender
        .let { tender ->
            PnCreateData.Tender(
                title = tender.title.takeIfNotEmpty {
                    ErrorException(
                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                        message = "The attribute 'tender.title' is empty or blank."
                    )
                },
                description = tender.description
                    .takeIfNotEmpty {
                        throw ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.description' is empty or blank."
                        )
                    },
                procurementMethodRationale = tender.procurementMethodRationale
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.procurementMethodRationale' is empty or blank."
                        )
                    },
                procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.procurementMethodAdditionalInfo' is empty or blank."
                        )
                    },
                tenderPeriod = tender.tenderPeriod
                    .let { tenderPeriod ->
                        PnCreateData.Tender.TenderPeriod(
                            startDate = tenderPeriod.startDate
                        )
                    },
                procuringEntity = tender.procuringEntity
                    ?.let { procuringEntity ->
                        PnCreateData.Tender.ProcuringEntity(
                            name = procuringEntity.name
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'procuringEntity.name' is empty or blank."
                                    )
                                },
                            identifier = procuringEntity.identifier
                                .let { identifier ->
                                    PnCreateData.Tender.ProcuringEntity.Identifier(
                                        scheme = identifier.scheme
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.scheme' is empty or blank."
                                                )
                                            },
                                        id = identifier.id
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.id' is empty or blank."
                                                )
                                            },
                                        legalName = identifier.legalName
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.legalName' is empty or blank."
                                                )
                                            },
                                        uri = identifier.uri
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.uri' is empty or blank."
                                                )
                                            }
                                    )
                                },
                            additionalIdentifiers = procuringEntity.additionalIdentifiers
                                .errorIfEmpty {
                                    ErrorException(
                                        error = ErrorType.IS_EMPTY,
                                        message = "The procuringEntity.additionalIdentifiers contains empty list of the additionalIdentifiers."
                                    )
                                }
                                ?.map { additionalIdentifiers ->
                                    PnCreateData.Tender.ProcuringEntity.AdditionalIdentifier(
                                        scheme = additionalIdentifiers.scheme
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'tender.procuringEntity.additionalIdentifiers.scheme' is empty or blank."
                                                )
                                            },
                                        id = additionalIdentifiers.id
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'tender.procuringEntity.additionalIdentifiers.id' is empty or blank."
                                                )
                                            },
                                        legalName = additionalIdentifiers.legalName
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'tender.procuringEntity.additionalIdentifiers.legalName' is empty or blank."
                                                )
                                            },
                                        uri = additionalIdentifiers.uri
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'tender.procuringEntity.additionalIdentifiers.uri' is empty or blank."
                                                )
                                            }
                                    )
                                }
                                .orEmpty(),
                            address = procuringEntity.address
                                .let { address ->
                                    PnCreateData.Tender.ProcuringEntity.Address(
                                        streetAddress = address.streetAddress
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'address.streetAddress' is empty or blank."
                                                )
                                            },
                                        postalCode = address.postalCode
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'address.postalCode' is empty or blank."
                                                )
                                            },
                                        addressDetails = address.addressDetails
                                            .let { addressDetails ->
                                                PnCreateData.Tender.ProcuringEntity.Address.AddressDetails(
                                                    country = addressDetails.country
                                                        .let { country ->
                                                            PnCreateData.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                                scheme = country.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = country.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = country.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = country.uri
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        },
                                                    region = addressDetails.region
                                                        .let { region ->
                                                            PnCreateData.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                                scheme = region.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = region.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = region.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = region.uri
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        },
                                                    locality = addressDetails.locality
                                                        .let { locality ->
                                                            PnCreateData.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                                                                scheme = locality.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = locality.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = locality.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = locality.uri
                                                                    ?.takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                },
                            contactPoint = procuringEntity.contactPoint
                                .let { contactPoint ->
                                    PnCreateData.Tender.ProcuringEntity.ContactPoint(
                                        name = contactPoint.name
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'contactPoint.name' is empty or blank."
                                                )
                                            },
                                        email = contactPoint.email
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'contactPoint.email' is empty or blank."
                                                )
                                            },
                                        telephone = contactPoint.telephone
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'contactPoint.telephone' is empty or blank."
                                                )
                                            },
                                        faxNumber = contactPoint.faxNumber
                                            ?.takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'contactPoint.faxNumber' is empty or blank."
                                                )
                                            },
                                        url = contactPoint.url
                                            ?.takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'contactPoint.url' is empty or blank."
                                                )
                                            }
                                    )
                                }
                        )
                    },
                lots = tender.lots
                    .errorIfEmpty {
                        ErrorException(
                            error = ErrorType.IS_EMPTY,
                            message = "The tender contain empty list of the lots."
                        )
                    }
                    ?.map { lot ->
                        PnCreateData.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'tender.lots.internalId' is empty or blank."
                                    )
                                },
                            title = lot.title
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'lot.title' is empty or blank."
                                    )
                                },
                            description = lot.description
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'lot.description' is empty or blank."
                                    )
                                },
                            value = lot.value,
                            contractPeriod = lot.contractPeriod
                                .let { contractPeriod ->
                                    PnCreateData.Tender.Lot.ContractPeriod(
                                        startDate = contractPeriod.startDate,
                                        endDate = contractPeriod.endDate
                                    )
                                },
                            placeOfPerformance = lot.placeOfPerformance
                                .let { placeOfPerformance ->
                                    PnCreateData.Tender.Lot.PlaceOfPerformance(
                                        address = placeOfPerformance.address.let { address ->
                                            PnCreateData.Tender.Lot.PlaceOfPerformance.Address(
                                                streetAddress = address.streetAddress
                                                    .takeIfNotEmpty {
                                                        ErrorException(
                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                            message = "The attribute 'address.streetAddress' is empty or blank."
                                                        )
                                                    },
                                                postalCode = address.postalCode
                                                    .takeIfNotEmpty {
                                                        ErrorException(
                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                            message = "The attribute 'address.postalCode' is empty or blank."
                                                        )
                                                    },
                                                addressDetails = address.addressDetails.let { addressDetails ->
                                                    PnCreateData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                        country = addressDetails.country.let { country ->
                                                            PnCreateData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                scheme = country.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = country.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = country.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = country.uri
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'country.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        },
                                                        region = addressDetails.region.let { region ->
                                                            PnCreateData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                scheme = region.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = region.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = region.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = region.uri
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'region.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        },
                                                        locality = addressDetails.locality.let { locality ->
                                                            PnCreateData.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                                scheme = locality.scheme
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.scheme' is empty or blank."
                                                                        )
                                                                    },
                                                                id = locality.id
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.id' is empty or blank."
                                                                        )
                                                                    },
                                                                description = locality.description
                                                                    .takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.description' is empty or blank."
                                                                        )
                                                                    },
                                                                uri = locality.uri
                                                                    ?.takeIfNotEmpty {
                                                                        ErrorException(
                                                                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                                            message = "The attribute 'locality.uri' is empty or blank."
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                    )
                                                }
                                            )
                                        },
                                        description = placeOfPerformance.description
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'tender.lots.placeOfPerformance.description' is empty or blank."
                                                )
                                            }
                                    )
                                }
                        )
                    }
                    .orEmpty(),
                items = tender.items
                    .errorIfEmpty {
                        ErrorException(
                            error = ErrorType.IS_EMPTY,
                            message = "The tender contain empty list of the items."
                        )
                    }
                    ?.map { item ->
                        PnCreateData.Tender.Item(
                            id = item.id
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'tender.items.id' is empty or blank."
                                    )
                                },
                            internalId = item.internalId
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'tender.items.internalId' is empty or blank."
                                    )
                                },
                            description = item.description
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'item.description' is empty or blank."
                                    )
                                },
                            relatedLot = item.relatedLot
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'item.relatedLot' is empty or blank."
                                    )
                                },
                            classification = item.classification
                                .let { classification ->
                                    PnCreateData.Tender.Item.Classification(
                                        id = classification.id,
                                        description = classification.description
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'classification.description' is empty or blank."
                                                )
                                            },
                                        scheme = classification.scheme
                                    )
                                },
                            additionalClassifications = item.additionalClassifications
                                .errorIfEmpty {
                                    ErrorException(
                                        error = ErrorType.IS_EMPTY,
                                        message = "The item contain empty list of the additionalClassifications."
                                    )
                                }
                                ?.map { additionalClassification ->
                                    PnCreateData.Tender.Item.AdditionalClassification(
                                        id = additionalClassification.id,
                                        description = additionalClassification.description
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'additionalClassification.description' is empty or blank."
                                                )
                                            },
                                        scheme = additionalClassification.scheme
                                    )
                                }
                                .orEmpty(),
                            quantity = item.quantity,
                            unit = item.unit
                                .let { unit ->
                                    PnCreateData.Tender.Item.Unit(
                                        id = unit.id,
                                        name = unit.name
                                    )
                                }
                        )
                    }
                    .orEmpty(),
                documents = tender.documents
                    .errorIfEmpty {
                        ErrorException(
                            error = ErrorType.IS_EMPTY,
                            message = "The tender contain empty list of the documents."
                        )
                    }
                    ?.map { document ->
                        PnCreateData.Tender.Document(
                            documentType = document.documentType,
                            id = document.id,
                            title = document.title
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'document.title' is empty or blank."
                                    )
                                },
                            description = document.description
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'document.description' is empty or blank."
                                    )
                                },
                            relatedLots = document.relatedLots?.toList().orEmpty()
                        )
                    }
                    .orEmpty(),
                classification = tender.classification
                    .let { classification ->
                        PnCreateData.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'tender.classification.description' is empty or blank."
                                    )
                                }
                        )
                    },
                eligibilityCriteria = tender.eligibilityCriteria
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.eligibilityCriteria' is empty or blank."
                        )
                    },
                legalBasis = tender.legalBasis,
                mainProcurementCategory = tender.mainProcurementCategory,
                procurementMethodDetails = tender.procurementMethodDetails
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.mainProcurementCategory' is empty or blank."
                        )
                    },
                submissionMethodDetails = tender.submissionMethodDetails
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.submissionMethodDetails' is empty or blank."
                        )
                    },
                submissionMethodRationale = tender.submissionMethodRationale.toList()
            )
        },
    buyer = this.buyer
        .let { buyer ->
            PnCreateData.Buyer(
                id = buyer.id,
                name = buyer.name,
                details = buyer.details
                    ?.let { details ->
                        PnCreateData.Buyer.Details(
                            typeOfBuyer = details.typeOfBuyer,
                            mainGeneralActivity = details.mainGeneralActivity,
                            mainSectoralActivity = details.mainSectoralActivity
                        )
                    },
                additionalIdentifiers = buyer.additionalIdentifiers
                    .errorIfEmpty {
                        ErrorException(
                            error = ErrorType.IS_EMPTY,
                            message = "Buyer contains empty list of additionalIdentifiers."
                        )
                    }
                    ?.map { additionalIdentifier ->
                        PnCreateData.Buyer.AdditionalIdentifier(
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            scheme = additionalIdentifier.scheme,
                            uri = additionalIdentifier.uri
                        )
                    }.orEmpty(),
                address = buyer.address
                    .let { address ->
                        PnCreateData.Buyer.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails.let { addressDetails ->
                                PnCreateData.Buyer.Address.AddressDetails(
                                    country = addressDetails.country.let { country ->
                                        PnCreateData.Buyer.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                    region = addressDetails.region.let { region ->
                                        PnCreateData.Buyer.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                    locality = addressDetails.locality.let { locality ->
                                        PnCreateData.Buyer.Address.AddressDetails.Locality(
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
                contactPoint = buyer.contactPoint
                    .let { contactPoint ->
                        PnCreateData.Buyer.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            faxNumber = contactPoint.faxNumber,
                            telephone = contactPoint.telephone,
                            url = contactPoint.url
                        )
                    },
                identifier = buyer.identifier
                    .let { identifier ->
                        PnCreateData.Buyer.Identifier(
                            id = identifier.id,
                            scheme = identifier.scheme,
                            uri = identifier.uri,
                            legalName = identifier.legalName
                        )
                    }
            )
        }
)
