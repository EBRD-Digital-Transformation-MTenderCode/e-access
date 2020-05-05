package com.procurement.access.infrastructure.dto.converter.get.organization

import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.handler.get.organization.GetOrganizationResult

fun convert(procuringEntity: CNEntity.Tender.ProcuringEntity): GetOrganizationResult =
    GetOrganizationResult(
        id = procuringEntity.id,
        identifier = procuringEntity.identifier
            .let { identifier ->
                GetOrganizationResult.Identifier(
                    id = identifier.id,
                    scheme = identifier.scheme,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = procuringEntity.additionalIdentifiers
            ?.map { additionalIdentifiers ->
                GetOrganizationResult.AdditionalIdentifier(
                    id = additionalIdentifiers.id,
                    scheme = additionalIdentifiers.scheme,
                    legalName = additionalIdentifiers.legalName,
                    uri = additionalIdentifiers.uri
                )
            },
        name = procuringEntity.name,
        contactPoint = procuringEntity.contactPoint
            .let { contactPoint ->
                GetOrganizationResult.ContactPoint(
                    name = contactPoint.name,
                    url = contactPoint.url,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber
                )
            },
        address = procuringEntity.address
            .let { address ->
                GetOrganizationResult.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            GetOrganizationResult.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        GetOrganizationResult.Address.AddressDetails.Country(
                                            id = country.id,
                                            scheme = country.scheme,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        GetOrganizationResult.Address.AddressDetails.Region(
                                            id = region.id,
                                            scheme = region.scheme,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        GetOrganizationResult.Address.AddressDetails.Locality(
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
        persons = procuringEntity.persones
            ?.map { person ->
                GetOrganizationResult.Person(
                    title = person.title,
                    name = person.name,
                    identifier = person.identifier
                        .let { identifier ->
                            GetOrganizationResult.Person.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                uri = identifier.uri
                            )
                        },
                    businessFunctions = person.businessFunctions
                        .map { businessFunction ->
                            GetOrganizationResult.Person.BusinessFunction(
                                id = businessFunction.id,
                                type = businessFunction.type,
                                jobTitle = businessFunction.jobTitle,
                                period = businessFunction.period
                                    .let { period ->
                                        GetOrganizationResult.Person.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                documents = businessFunction.documents
                                    ?.map { document ->
                                        GetOrganizationResult.Person.BusinessFunction.Document(
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

