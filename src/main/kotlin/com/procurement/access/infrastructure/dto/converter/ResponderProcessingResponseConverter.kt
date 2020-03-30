package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingResult

fun CNEntity.Tender.ProcuringEntity.convert(): ResponderProcessingResult =
    ResponderProcessingResult(
        id = this.id,
        identifier = this.identifier
            .let { identifier ->
                ResponderProcessingResult.Identifier(
                    id = identifier.id,
                    scheme = identifier.scheme,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = this.additionalIdentifiers
            ?.map { additionalIdentifiers ->
                ResponderProcessingResult.AdditionalIdentifier(
                    id = additionalIdentifiers.id,
                    scheme = additionalIdentifiers.scheme,
                    legalName = additionalIdentifiers.legalName,
                    uri = additionalIdentifiers.legalName
                )
            },
        name = this.name,
        contactPoint = this.contactPoint
            .let { contactPoint ->
                ResponderProcessingResult.ContactPoint(
                    name = contactPoint.name,
                    url = contactPoint.url,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber
                )
            },
        address = this.address
            .let { address ->
                ResponderProcessingResult.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            ResponderProcessingResult.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        ResponderProcessingResult.Address.AddressDetails.Country(
                                            id = country.id,
                                            scheme = country.scheme,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        ResponderProcessingResult.Address.AddressDetails.Region(
                                            id = region.id,
                                            scheme = region.scheme,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        ResponderProcessingResult.Address.AddressDetails.Locality(
                                            id = locality.id,
                                            scheme = locality.scheme,
                                            description = locality.description,
                                            uri = locality.uri
                                        )
                                    }
                            )
                        }
                )
            },
        persons = this.persones
            ?.map { person ->
                ResponderProcessingResult.Person(
                    title = person.title,
                    name = person.name,
                    identifier = person.identifier
                        .let { identifier ->
                            ResponderProcessingResult.Person.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                uri = identifier.uri
                            )
                        },
                    businessFunctions = person.businessFunctions
                        .map { businessFunction ->
                            ResponderProcessingResult.Person.BusinessFunction(
                                id = businessFunction.id,
                                type = businessFunction.type,
                                jobTitle = businessFunction.jobTitle,
                                period = businessFunction.period
                                    .let { period ->
                                        ResponderProcessingResult.Person.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                documents = businessFunction.documents
                                    ?.map { document ->
                                        ResponderProcessingResult.Person.BusinessFunction.Document(
                                            id = document.id,
                                            title = document.title,
                                            description = document.description,
                                            documentType = document.documentType
                                        )
                                    }
                            )
                        }
                )
            }
    )

