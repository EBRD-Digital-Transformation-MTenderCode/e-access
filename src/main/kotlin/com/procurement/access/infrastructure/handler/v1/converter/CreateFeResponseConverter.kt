package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.infrastructure.handler.v1.model.response.CreateFEResponse

fun CreateFEResult.convert(): CreateFEResponse =
    CreateFEResponse(
        ocid = this.ocid,
        token = this.token,
        tender = this.tender.convert(),
        parties = this.parties.map { it.convert() }
    )

fun CreateFEResult.Tender.convert(): CreateFEResponse.Tender =
    CreateFEResponse.Tender(
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
        documents = this.documents.map { it.convert() },
        procurementMethodModalities = this.procurementMethodModalities,
        mainProcurementCategory = this.mainProcurementCategory,
        value = this.value,
        secondStage = this.secondStage?.convert(),
        otherCriteria = this.otherCriteria.convert(),
        contractPeriod = this.contractPeriod.convert(),
        criteria = this.criteria.map { it.convert() }
    )

fun CreateFEResult.Tender.Classification.convert(): CreateFEResponse.Tender.Classification =
    CreateFEResponse.Tender.Classification(
        scheme = this.scheme,
        id = this.id,
        description = this.description
    )

fun CreateFEResult.Tender.AcceleratedProcedure.convert(): CreateFEResponse.Tender.AcceleratedProcedure =
    CreateFEResponse.Tender.AcceleratedProcedure(
        isAcceleratedProcedure = this.isAcceleratedProcedure
    )

fun CreateFEResult.Tender.DesignContest.convert(): CreateFEResponse.Tender.DesignContest =
    CreateFEResponse.Tender.DesignContest(
        serviceContractAward = this.serviceContractAward
    )

fun CreateFEResult.Tender.ElectronicWorkflows.convert(): CreateFEResponse.Tender.ElectronicWorkflows =
    CreateFEResponse.Tender.ElectronicWorkflows(
        useOrdering = this.useOrdering,
        usePayment = this.usePayment,
        acceptInvoicing = this.acceptInvoicing
    )

fun CreateFEResult.Tender.JointProcurement.convert(): CreateFEResponse.Tender.JointProcurement =
    CreateFEResponse.Tender.JointProcurement(
        isJointProcurement = this.isJointProcurement
    )

fun CreateFEResult.Tender.ProcedureOutsourcing.convert(): CreateFEResponse.Tender.ProcedureOutsourcing =
    CreateFEResponse.Tender.ProcedureOutsourcing(
        procedureOutsourced = this.procedureOutsourced
    )

fun CreateFEResult.Tender.Framework.convert(): CreateFEResponse.Tender.Framework =
    CreateFEResponse.Tender.Framework(
        isAFramework = this.isAFramework
    )

fun CreateFEResult.Tender.DynamicPurchasingSystem.convert(): CreateFEResponse.Tender.DynamicPurchasingSystem =
    CreateFEResponse.Tender.DynamicPurchasingSystem(
        hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
    )

fun CreateFEResult.Tender.Document.convert(): CreateFEResponse.Tender.Document =
    CreateFEResponse.Tender.Document(
        documentType = this.documentType,
        id = this.id,
        title = this.title,
        description = this.description
    )

fun CreateFEResult.Tender.SecondStage.convert(): CreateFEResponse.Tender.SecondStage =
    CreateFEResponse.Tender.SecondStage(
        minimumCandidates = this.minimumCandidates,
        maximumCandidates = this.maximumCandidates
    )

fun CreateFEResult.Tender.OtherCriteria.convert(): CreateFEResponse.Tender.OtherCriteria =
    CreateFEResponse.Tender.OtherCriteria(
        reductionCriteria = this.reductionCriteria,
        qualificationSystemMethods = this.qualificationSystemMethods
    )

fun CreateFEResult.Tender.ContractPeriod.convert(): CreateFEResponse.Tender.ContractPeriod =
    CreateFEResponse.Tender.ContractPeriod(
        startDate = this.startDate,
        endDate = this.endDate
    )

fun CreateFEResult.Tender.Criteria.convert(): CreateFEResponse.Tender.Criteria =
    CreateFEResponse.Tender.Criteria(
        id = this.id,
        title = this.title,
        description = this.description,
        relatesTo = this.relatesTo,
        source = this.source,
        requirementGroups = this.requirementGroups.map { it.convert() },
        classification = this.classification
            ?.let { classification ->
                CreateFEResponse.Tender.Criteria.Classification(
                    id = classification.id,
                    scheme = classification.scheme
                )
            }
    )

fun CreateFEResult.Tender.Criteria.RequirementGroup.convert(): CreateFEResponse.Tender.Criteria.RequirementGroup =
    CreateFEResponse.Tender.Criteria.RequirementGroup(
        id = this.id,
        description = this.description,
        requirements = this.requirements
    )

fun CreateFEResult.Tender.ProcuringEntity.convert(): CreateFEResponse.Tender.ProcuringEntity =
    CreateFEResponse.Tender.ProcuringEntity(
        id = this.id,
        name = this.name
    )

private fun CreateFEResult.Party.convert(): CreateFEResponse.Party =
    CreateFEResponse.Party(
        id = id,
        name = name,
        identifier = identifier
            .let { identifier ->
                CreateFEResponse.Party.Identifier(
                    scheme = identifier.scheme,
                    id = identifier.id,
                    legalName = identifier.legalName,
                    uri = identifier.uri
                )
            },
        additionalIdentifiers = additionalIdentifiers
            ?.map { additionalIdentifier ->
                CreateFEResponse.Party.AdditionalIdentifier(
                    scheme = additionalIdentifier.scheme,
                    id = additionalIdentifier.id,
                    legalName = additionalIdentifier.legalName,
                    uri = additionalIdentifier.uri
                )
            },
        address = address
            .let { address ->
                CreateFEResponse.Party.Address(
                    streetAddress = address.streetAddress,
                    postalCode = address.postalCode,
                    addressDetails = address.addressDetails
                        .let { addressDetails ->
                            CreateFEResponse.Party.Address.AddressDetails(
                                country = addressDetails.country
                                    .let { country ->
                                        CreateFEResponse.Party.Address.AddressDetails.Country(
                                            scheme = country.scheme,
                                            id = country.id,
                                            description = country.description,
                                            uri = country.uri
                                        )
                                    },
                                region = addressDetails.region
                                    .let { region ->
                                        CreateFEResponse.Party.Address.AddressDetails.Region(
                                            scheme = region.scheme,
                                            id = region.id,
                                            description = region.description,
                                            uri = region.uri
                                        )
                                    },
                                locality = addressDetails.locality
                                    .let { locality ->
                                        CreateFEResponse.Party.Address.AddressDetails.Locality(
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
                CreateFEResponse.Party.ContactPoint(
                    name = contactPoint.name,
                    email = contactPoint.email,
                    telephone = contactPoint.telephone,
                    faxNumber = contactPoint.faxNumber,
                    url = contactPoint.url
                )
            },
        roles = roles,
        persones = persones?.map { person ->
            CreateFEResponse.Party.Person(
                id = person.id,
                title = person.title,
                name = person.name,
                identifier = person.identifier
                    .let { identifier ->
                        CreateFEResponse.Party.Person.Identifier(
                            id = identifier.id,
                            scheme = identifier.scheme,
                            uri = identifier.uri
                        )
                    },
                businessFunctions = person.businessFunctions
                    .map { businessFunctions ->
                        CreateFEResponse.Party.Person.BusinessFunction(
                            id = businessFunctions.id,
                            jobTitle = businessFunctions.jobTitle,
                            type = businessFunctions.type,
                            period = businessFunctions.period
                                .let { period ->
                                    CreateFEResponse.Party.Person.BusinessFunction.Period(
                                        startDate = period.startDate
                                    )
                                },
                            documents = businessFunctions.documents
                                ?.map { document ->
                                    CreateFEResponse.Party.Person.BusinessFunction.Document(
                                        id = document.id,
                                        title = document.title,
                                        description = document.description,
                                        documentType = document.documentType
                                    )
                                }
                        )
                    }
            )
        },
        details = details?.let { details ->
            CreateFEResponse.Party.Details(
                typeOfBuyer = details.typeOfBuyer,
                mainSectoralActivity = details.mainSectoralActivity,
                mainGeneralActivity = details.mainGeneralActivity
            )
        }
    )
