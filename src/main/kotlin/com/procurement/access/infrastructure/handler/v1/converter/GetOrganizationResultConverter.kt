package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v2.model.response.GetOrganizationsResult

fun convert(procuringEntity: CNEntity.Tender.ProcuringEntity): GetOrganizationsResult.Party =
    GetOrganizationsResult.Party(
        id = procuringEntity.id,
        identifier = procuringEntity.identifier
            .let { identifier ->
                GetOrganizationsResult.Party.Identifier(
                    id = identifier.id,
                    scheme = identifier.scheme,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = procuringEntity.additionalIdentifiers
            ?.map { additionalIdentifiers ->
                GetOrganizationsResult.Party.AdditionalIdentifier(
                    id = additionalIdentifiers.id,
                    scheme = additionalIdentifiers.scheme,
                    legalName = additionalIdentifiers.legalName,
                    uri = additionalIdentifiers.uri
                )
            },
        name = procuringEntity.name,
        contactPoint = procuringEntity.contactPoint
            .let { contactPoint ->
                GetOrganizationsResult.Party.ContactPoint(
                    name = contactPoint.name,
                    url = contactPoint.url,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber
                )
            },
        address = procuringEntity.address
            .let { address ->
                GetOrganizationsResult.Party.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            GetOrganizationsResult.Party.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Country(
                                            id = country.id,
                                            scheme = country.scheme,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Region(
                                            id = region.id,
                                            scheme = region.scheme,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Locality(
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
                GetOrganizationsResult.Party.Person(
                    id = person.id,
                    title = person.title,
                    name = person.name,
                    identifier = person.identifier
                        .let { identifier ->
                            GetOrganizationsResult.Party.Person.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                uri = identifier.uri
                            )
                        },
                    businessFunctions = person.businessFunctions
                        .map { businessFunction ->
                            GetOrganizationsResult.Party.Person.BusinessFunction(
                                id = businessFunction.id,
                                type = businessFunction.type,
                                jobTitle = businessFunction.jobTitle,
                                period = businessFunction.period
                                    .let { period ->
                                        GetOrganizationsResult.Party.Person.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                documents = businessFunction.documents
                                    ?.map { document ->
                                        GetOrganizationsResult.Party.Person.BusinessFunction.Document(
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

fun convert(procuringEntity: FEEntity.Party): GetOrganizationsResult.Party =
    GetOrganizationsResult.Party(
        id = procuringEntity.id,
        identifier = procuringEntity.identifier
            .let { identifier ->
                GetOrganizationsResult.Party.Identifier(
                    id = identifier.id,
                    scheme = identifier.scheme,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = procuringEntity.additionalIdentifiers
            ?.map { additionalIdentifiers ->
                GetOrganizationsResult.Party.AdditionalIdentifier(
                    id = additionalIdentifiers.id,
                    scheme = additionalIdentifiers.scheme,
                    legalName = additionalIdentifiers.legalName,
                    uri = additionalIdentifiers.uri
                )
            },
        name = procuringEntity.name,
        contactPoint = procuringEntity.contactPoint
            .let { contactPoint ->
                GetOrganizationsResult.Party.ContactPoint(
                    name = contactPoint.name,
                    url = contactPoint.url,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber
                )
            },
        address = procuringEntity.address
            .let { address ->
                GetOrganizationsResult.Party.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            GetOrganizationsResult.Party.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Country(
                                            id = country.id,
                                            scheme = country.scheme,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Region(
                                            id = region.id,
                                            scheme = region.scheme,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        GetOrganizationsResult.Party.Address.AddressDetails.Locality(
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
                GetOrganizationsResult.Party.Person(
                    id = person.id,
                    title = person.title,
                    name = person.name,
                    identifier = person.identifier
                        .let { identifier ->
                            GetOrganizationsResult.Party.Person.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                uri = identifier.uri
                            )
                        },
                    businessFunctions = person.businessFunctions
                        .map { businessFunction ->
                            GetOrganizationsResult.Party.Person.BusinessFunction(
                                id = businessFunction.id,
                                type = businessFunction.type,
                                jobTitle = businessFunction.jobTitle,
                                period = businessFunction.period
                                    .let { period ->
                                        GetOrganizationsResult.Party.Person.BusinessFunction.Period(
                                            startDate = period.startDate
                                        )
                                    },
                                documents = businessFunction.documents
                                    ?.map { document ->
                                        GetOrganizationsResult.Party.Person.BusinessFunction.Document(
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