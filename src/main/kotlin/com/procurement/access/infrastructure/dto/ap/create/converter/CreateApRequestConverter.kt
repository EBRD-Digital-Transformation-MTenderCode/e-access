package com.procurement.access.infrastructure.dto.ap.create.converter

import com.procurement.access.application.service.ap.create.ApCreateData
import com.procurement.access.domain.EnumElementProviderParser.checkAndParseEnum
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.ap.create.ApCreateRequest
import com.procurement.access.lib.errorIfEmpty
import com.procurement.access.lib.takeIfNotEmpty

fun ApCreateRequest.convert() = ApCreateData(
    tender = this.tender
        .let { tender ->
            ApCreateData.Tender(
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
                tenderPeriod = tender.tenderPeriod
                    .let { tenderPeriod ->
                        ApCreateData.Tender.TenderPeriod(
                            startDate = tenderPeriod.startDate
                        )
                    },
                contractPeriod = tender.contractPeriod
                    .let { contractPeriod ->
                        ApCreateData.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                procuringEntity = tender.procuringEntity
                    .let { procuringEntity ->
                        ApCreateData.Tender.ProcuringEntity(
                            name = procuringEntity.name
                                .takeIfNotEmpty {
                                    ErrorException(
                                        error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                        message = "The attribute 'procuringEntity.name' is empty or blank."
                                    )
                                },
                            identifier = procuringEntity.identifier
                                .let { identifier ->
                                    ApCreateData.Tender.ProcuringEntity.Identifier(
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
                                    ApCreateData.Tender.ProcuringEntity.AdditionalIdentifier(
                                        scheme = additionalIdentifiers.scheme
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.scheme' is empty or blank."
                                                )
                                            },
                                        id = additionalIdentifiers.id
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.id' is empty or blank."
                                                )
                                            },
                                        legalName = additionalIdentifiers.legalName
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.legalName' is empty or blank."
                                                )
                                            },
                                        uri = additionalIdentifiers.uri
                                            .takeIfNotEmpty {
                                                ErrorException(
                                                    error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                                                    message = "The attribute 'identifier.uri' is empty or blank."
                                                )
                                            }
                                    )
                                }
                                .orEmpty(),
                            address = procuringEntity.address
                                .let { address ->
                                    ApCreateData.Tender.ProcuringEntity.Address(
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
                                                ApCreateData.Tender.ProcuringEntity.Address.AddressDetails(
                                                    country = addressDetails.country
                                                        .let { country ->
                                                            ApCreateData.Tender.ProcuringEntity.Address.AddressDetails.Country(
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
                                                            ApCreateData.Tender.ProcuringEntity.Address.AddressDetails.Region(
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
                                                            ApCreateData.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                    ApCreateData.Tender.ProcuringEntity.ContactPoint(
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
                documents = tender.documents
                    .errorIfEmpty {
                        ErrorException(
                            error = ErrorType.IS_EMPTY,
                            message = "The tender contain empty list of the documents."
                        )
                    }
                    ?.map { document ->
                        ApCreateData.Tender.Document(
                            documentType = parseTenderDocumentType(document.documentType),
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
                                }
                        )
                    }
                    .orEmpty(),
                classification = tender.classification
                    .let { classification ->
                        ApCreateData.Tender.Classification(
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
                value = ApCreateData.Tender.Value(tender.value.currency),
                eligibilityCriteria = tender.eligibilityCriteria
                    .takeIfNotEmpty {
                        ErrorException(
                            error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                            message = "The attribute 'tender.eligibilityCriteria' is empty or blank."
                        )
                    },
                legalBasis = tender.legalBasis,
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
        }
)

private val allowedTenderDocumentTypes = DocumentType.allowedElements
    .filter {
        when (it) {
            DocumentType.TENDER_NOTICE,
            DocumentType.BIDDING_DOCUMENTS,
            DocumentType.TECHNICAL_SPECIFICATIONS,
            DocumentType.EVALUATION_CRITERIA,
            DocumentType.CLARIFICATIONS,
            DocumentType.ELIGIBILITY_CRITERIA,
            DocumentType.RISK_PROVISIONS,
            DocumentType.BILL_OF_QUANTITY,
            DocumentType.CONFLICT_OF_INTEREST,
            DocumentType.PROCUREMENT_PLAN,
            DocumentType.CONTRACT_DRAFT,
            DocumentType.COMPLAINTS,
            DocumentType.ILLUSTRATION,
            DocumentType.CANCELLATION_DETAILS,
            DocumentType.EVALUATION_REPORTS,
            DocumentType.SHORTLISTED_FIRMS,
            DocumentType.CONTRACT_ARRANGEMENTS,
            DocumentType.CONTRACT_GUARANTEES -> true

            DocumentType.ASSET_AND_LIABILITY_ASSESSMENT,
            DocumentType.ENVIRONMENTAL_IMPACT,
            DocumentType.FEASIBILITY_STUDY,
            DocumentType.HEARING_NOTICE,
            DocumentType.MARKET_STUDIES,
            DocumentType.NEEDS_ASSESSMENT,
            DocumentType.PROJECT_PLAN -> false
        }
    }.toSet()

private fun parseTenderDocumentType(documentType: String) =
    checkAndParseEnum(documentType, allowedTenderDocumentTypes, DocumentType)


