package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.update.AmendFEResult
import com.procurement.access.infrastructure.handler.v1.model.response.AmendFEResponse

fun AmendFEResult.convert(): AmendFEResponse =
    AmendFEResponse(
        tender = this.tender.convert(),
        parties = this.parties.map { it.convert() }
    )

fun AmendFEResult.Tender.convert(): AmendFEResponse.Tender =
    AmendFEResponse.Tender(
        id = this.id,
        status = this.status,
        statusDetails = this.statusDetails,
        title = this.title,
        description = this.description,
        classification = this.classification.convert(),
        acceleratedProcedure = this.acceleratedProcedure.convert(),
        designContest = this.designContest.convert(),
        electronicWorkflows = this.electronicWorkflows.convert(),
        jointProcurement = this.jointProcurement.convert(),
        procedureOutsourcing = this.procedureOutsourcing.convert(),
        framework = this.framework.convert(),
        dynamicPurchasingSystem = this.dynamicPurchasingSystem.convert(),
        legalBasis = this.legalBasis,
        procurementMethod = this.procurementMethod,
        procurementMethodDetails = this.procurementMethodDetails,
        procurementMethodRationale = this.procurementMethodRationale,
        eligibilityCriteria = this.eligibilityCriteria,
        procuringEntity = this.procuringEntity.convert(),
        requiresElectronicCatalogue = this.requiresElectronicCatalogue,
        submissionMethod = this.submissionMethod.toList(),
        submissionMethodRationale = this.submissionMethodRationale.toList(),
        submissionMethodDetails = this.submissionMethodDetails,
        documents = this.documents?.map { it.convert() },
        procurementMethodModalities = this.procurementMethodModalities,
        mainProcurementCategory = this.mainProcurementCategory,
        value = this.value,
        secondStage = this.secondStage?.convert(),
        otherCriteria = this.otherCriteria.convert(),
        contractPeriod = this.contractPeriod.convert(),
        criteria = this.criteria?.map { it.convert() }
    )

fun AmendFEResult.Tender.Classification.convert(): AmendFEResponse.Tender.Classification =
    AmendFEResponse.Tender.Classification(
        scheme = this.scheme,
        id = this.id,
        description = this.description
    )

fun AmendFEResult.Tender.AcceleratedProcedure.convert(): AmendFEResponse.Tender.AcceleratedProcedure =
    AmendFEResponse.Tender.AcceleratedProcedure(
        isAcceleratedProcedure = this.isAcceleratedProcedure
    )

fun AmendFEResult.Tender.DesignContest.convert(): AmendFEResponse.Tender.DesignContest =
    AmendFEResponse.Tender.DesignContest(
        serviceContractAward = this.serviceContractAward
    )

fun AmendFEResult.Tender.ElectronicWorkflows.convert(): AmendFEResponse.Tender.ElectronicWorkflows =
    AmendFEResponse.Tender.ElectronicWorkflows(
        useOrdering = this.useOrdering,
        usePayment = this.usePayment,
        acceptInvoicing = this.acceptInvoicing
    )

fun AmendFEResult.Tender.JointProcurement.convert(): AmendFEResponse.Tender.JointProcurement =
    AmendFEResponse.Tender.JointProcurement(
        isJointProcurement = this.isJointProcurement
    )

fun AmendFEResult.Tender.ProcedureOutsourcing.convert(): AmendFEResponse.Tender.ProcedureOutsourcing =
    AmendFEResponse.Tender.ProcedureOutsourcing(
        procedureOutsourced = this.procedureOutsourced
    )

fun AmendFEResult.Tender.Framework.convert(): AmendFEResponse.Tender.Framework =
    AmendFEResponse.Tender.Framework(
        isAFramework = this.isAFramework
    )

fun AmendFEResult.Tender.DynamicPurchasingSystem.convert(): AmendFEResponse.Tender.DynamicPurchasingSystem =
    AmendFEResponse.Tender.DynamicPurchasingSystem(
        hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
    )

fun AmendFEResult.Tender.Document.convert(): AmendFEResponse.Tender.Document =
    AmendFEResponse.Tender.Document(
        documentType = this.documentType,
        id = this.id,
        title = this.title,
        description = this.description
    )

fun AmendFEResult.Tender.SecondStage.convert(): AmendFEResponse.Tender.SecondStage =
    AmendFEResponse.Tender.SecondStage(
        minimumCandidates = this.minimumCandidates,
        maximumCandidates = this.maximumCandidates
    )

fun AmendFEResult.Tender.OtherCriteria.convert(): AmendFEResponse.Tender.OtherCriteria =
    AmendFEResponse.Tender.OtherCriteria(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun AmendFEResult.Tender.ContractPeriod.convert(): AmendFEResponse.Tender.ContractPeriod =
    AmendFEResponse.Tender.ContractPeriod(
        startDate = this.startDate,
        endDate = this.endDate
    )

fun AmendFEResult.Tender.Criteria.convert(): AmendFEResponse.Tender.Criteria =
    AmendFEResponse.Tender.Criteria(
        id = this.id,
        title = this.title,
        description = this.description,
        relatesTo = this.relatesTo,
        source = this.source,
        classification = this.classification?.convert(),
        requirementGroups = this.requirementGroups.map { it.convert() }
    )

fun AmendFEResult.Tender.Criteria.Classification.convert(): AmendFEResponse.Tender.Criteria.Classification =
    AmendFEResponse.Tender.Criteria.Classification(
        id = this.id,
        scheme = this.scheme
    )

fun AmendFEResult.Tender.Criteria.RequirementGroup.convert(): AmendFEResponse.Tender.Criteria.RequirementGroup =
    AmendFEResponse.Tender.Criteria.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun AmendFEResult.Tender.ProcuringEntity.convert(): AmendFEResponse.Tender.ProcuringEntity =
    AmendFEResponse.Tender.ProcuringEntity(
        id = this.id,
        name = this.name
    )

private fun AmendFEResult.Party.convert(): AmendFEResponse.Party =
    AmendFEResponse.Party(
        id = id,
        name = name,
        identifier = identifier
            .let { identifier ->
                AmendFEResponse.Party.Identifier(
                    scheme = identifier.scheme,
                    id = identifier.id,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = additionalIdentifiers
            ?.map { additionalIdentifier ->
                AmendFEResponse.Party.AdditionalIdentifier(
                    scheme = additionalIdentifier.scheme,
                    id = additionalIdentifier.id,
                    legalName = additionalIdentifier.legalName,
                    uri = additionalIdentifier.uri
                )
            },
        address = address
            .let { address ->
                AmendFEResponse.Party.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            AmendFEResponse.Party.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        AmendFEResponse.Party.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        AmendFEResponse.Party.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        AmendFEResponse.Party.Address.AddressDetails.Locality(
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
        contactPoint = contactPoint
            .let { contactPoint ->
                AmendFEResponse.Party.ContactPoint(
                    name = contactPoint.name,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber,
                    url = contactPoint.url
                )
            },
        roles = roles,
        persones = persones?.map { person ->
            AmendFEResponse.Party.Person(
                id = person.id,
                title = person.title,
                name = person.name,
                identifier = person.identifier
                    .let { identifier ->
                        AmendFEResponse.Party.Person.Identifier(
                            id = identifier.id,
                            scheme = identifier.scheme,
                            uri = identifier.uri
                        )
                    },
                businessFunctions = person.businessFunctions
                    .map { businessFunctions ->
                        AmendFEResponse.Party.Person.BusinessFunction(
                            id = businessFunctions.id,
                            jobTitle = businessFunctions.jobTitle,
                            type = businessFunctions.type,
                            period = businessFunctions.period
                                .let { period ->
                                    AmendFEResponse.Party.Person.BusinessFunction.Period(
                                        startDate = period.startDate
                                    )
                                },
                            documents = businessFunctions.documents
                                ?.map { document ->
                                    AmendFEResponse.Party.Person.BusinessFunction.Document(
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