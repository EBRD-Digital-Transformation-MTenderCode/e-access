package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.ap.create.ApCreateResult
import com.procurement.access.infrastructure.handler.v1.model.response.ApCreateResponse

fun ApCreateResult.convert(): ApCreateResponse =
    ApCreateResponse(
        cpid = this.cpid.value,
        ocid = this.ocid,
        token = this.token,
        tender = this.tender
            .let { tender ->
                ApCreateResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification
                        .let { classification ->
                            ApCreateResponse.Tender.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                    value = ApCreateResponse.Tender.Value(tender.value.currency),
                    tenderPeriod = tender.tenderPeriod
                        .let { tenderPeriod ->
                            ApCreateResponse.Tender.TenderPeriod(
                                startDate = tenderPeriod.startDate
                            )
                        },
                    contractPeriod = tender.contractPeriod
                        .let { contractPeriod ->
                            ApCreateResponse.Tender.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                    acceleratedProcedure = tender.acceleratedProcedure
                        .let { acceleratedProcedure ->
                            ApCreateResponse.Tender.AcceleratedProcedure(
                                isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                            )
                        },
                    designContest = tender.designContest
                        .let { designContest ->
                            ApCreateResponse.Tender.DesignContest(
                                serviceContractAward = designContest.serviceContractAward
                            )
                        },
                    electronicWorkflows = tender.electronicWorkflows
                        .let { electronicWorkflows ->
                            ApCreateResponse.Tender.ElectronicWorkflows(
                                useOrdering = electronicWorkflows.useOrdering,
                                usePayment = electronicWorkflows.usePayment,
                                acceptInvoicing = electronicWorkflows.acceptInvoicing
                            )
                        },
                    jointProcurement = tender.jointProcurement
                        .let { jointProcurement ->
                            ApCreateResponse.Tender.JointProcurement(
                                isJointProcurement = jointProcurement.isJointProcurement
                            )
                        },
                    procedureOutsourcing = tender.procedureOutsourcing
                        .let { procedureOutsourcing ->
                            ApCreateResponse.Tender.ProcedureOutsourcing(
                                procedureOutsourced = procedureOutsourcing.procedureOutsourced
                            )
                        },
                    framework = tender.framework
                        .let { framework ->
                            ApCreateResponse.Tender.Framework(
                                isAFramework = framework.isAFramework
                            )
                        },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                        .let { dynamicPurchasingSystem ->
                            ApCreateResponse.Tender.DynamicPurchasingSystem(
                                hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                            )
                        },
                    legalBasis = tender.legalBasis,
                    procurementMethod = tender.procurementMethod,
                    procurementMethodDetails = tender.procurementMethodDetails,
                    procurementMethodRationale = tender.procurementMethodRationale,
                    eligibilityCriteria = tender.eligibilityCriteria,
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    submissionMethod = tender.submissionMethod.toList(),
                    submissionMethodRationale = tender.submissionMethodRationale.toList(),
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents
                        .map { document ->
                            ApCreateResponse.Tender.Document(
                                documentType = document.documentType,
                                id = document.id,
                                title = document.title,
                                description = document.description
                            )
                        }
                )
            },
        parties = this.parties
            .map { party ->
                ApCreateResponse.Party(
                    id = party.id,
                    name = party.name,
                    identifier = party.identifier
                        .let { identifier ->
                            ApCreateResponse.Party.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                    additionalIdentifiers = party.additionalIdentifiers
                        .map { additionalIdentifier ->
                            ApCreateResponse.Party.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                    address = party.address
                        .let { address ->
                            ApCreateResponse.Party.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails
                                    .let { addressDetails ->
                                        ApCreateResponse.Party.Address.AddressDetails(
                                            country = addressDetails.country
                                                .let { country ->
                                                    ApCreateResponse.Party.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                            region = addressDetails.region
                                                .let { region ->
                                                    ApCreateResponse.Party.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                            locality = addressDetails.locality
                                                .let { locality ->
                                                    ApCreateResponse.Party.Address.AddressDetails.Locality(
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
                    contactPoint = party.contactPoint
                        .let { contactPoint ->
                            ApCreateResponse.Party.ContactPoint(
                                name = contactPoint.name,
                                email = contactPoint.email,
                                telephone = contactPoint.telephone,
                                faxNumber = contactPoint.faxNumber,
                                url = contactPoint.url
                            )
                        },
                    roles = party.roles
                )
            }
    )
