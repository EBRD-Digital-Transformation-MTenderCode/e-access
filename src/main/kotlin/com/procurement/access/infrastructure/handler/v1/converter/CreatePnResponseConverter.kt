package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.pn.create.PnCreateResult
import com.procurement.access.infrastructure.handler.v1.model.response.PnCreateResponse

fun PnCreateResult.convert(): PnCreateResponse =
    PnCreateResponse(
        ocid = this.ocid,
        token = this.token,
        planning = this.planning
            .let { planning ->
                PnCreateResponse.Planning(
                    rationale = planning.rationale,
                    budget = planning.budget
                        .let { budget ->
                            PnCreateResponse.Planning.Budget(
                                description = budget.description,
                                amount = budget.amount,
                                isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                                budgetBreakdowns = budget.budgetBreakdowns
                                    .map { budgetBreakdown ->
                                        PnCreateResponse.Planning.Budget.BudgetBreakdown(
                                            id = budgetBreakdown.id,
                                            description = budgetBreakdown.description,
                                            amount = budgetBreakdown.amount,
                                            period = budgetBreakdown.period
                                                .let { period ->
                                                    PnCreateResponse.Planning.Budget.BudgetBreakdown.Period(
                                                        startDate = period.startDate,
                                                        endDate = period.endDate
                                                    )
                                                },
                                            sourceParty = budgetBreakdown.sourceParty
                                                .let { sourceParty ->
                                                    PnCreateResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                                        id = sourceParty.id,
                                                        name = sourceParty.name
                                                    )
                                                },
                                            europeanUnionFunding = budgetBreakdown.europeanUnionFunding
                                                ?.let { europeanUnionFunding ->
                                                    PnCreateResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                                        projectIdentifier = europeanUnionFunding.projectIdentifier,
                                                        projectName = europeanUnionFunding.projectName,
                                                        uri = europeanUnionFunding.uri
                                                    )
                                                }
                                        )
                                    }
                            )
                        }
                )
            },
        tender = this.tender
            .let { tender ->
                PnCreateResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification
                        .let { classification ->
                            PnCreateResponse.Tender.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                    tenderPeriod = tender.tenderPeriod
                        .let { tenderPeriod ->
                            PnCreateResponse.Tender.TenderPeriod(
                                startDate = tenderPeriod.startDate
                            )
                        },
                    acceleratedProcedure = tender.acceleratedProcedure
                        .let { acceleratedProcedure ->
                            PnCreateResponse.Tender.AcceleratedProcedure(
                                isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                            )
                        },
                    designContest = tender.designContest
                        .let { designContest ->
                            PnCreateResponse.Tender.DesignContest(
                                serviceContractAward = designContest.serviceContractAward
                            )
                        },
                    electronicWorkflows = tender.electronicWorkflows
                        .let { electronicWorkflows ->
                            PnCreateResponse.Tender.ElectronicWorkflows(
                                useOrdering = electronicWorkflows.useOrdering,
                                usePayment = electronicWorkflows.usePayment,
                                acceptInvoicing = electronicWorkflows.acceptInvoicing
                            )
                        },
                    jointProcurement = tender.jointProcurement
                        .let { jointProcurement ->
                            PnCreateResponse.Tender.JointProcurement(
                                isJointProcurement = jointProcurement.isJointProcurement
                            )
                        },
                    procedureOutsourcing = tender.procedureOutsourcing
                        .let { procedureOutsourcing ->
                            PnCreateResponse.Tender.ProcedureOutsourcing(
                                procedureOutsourced = procedureOutsourcing.procedureOutsourced
                            )
                        },
                    framework = tender.framework
                        .let { framework ->
                            PnCreateResponse.Tender.Framework(
                                isAFramework = framework.isAFramework
                            )
                        },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                        .let { dynamicPurchasingSystem ->
                            PnCreateResponse.Tender.DynamicPurchasingSystem(
                                hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                            )
                        },
                    legalBasis = tender.legalBasis,
                    procurementMethod = tender.procurementMethod,
                    procurementMethodDetails = tender.procurementMethodDetails,
                    procurementMethodRationale = tender.procurementMethodRationale,
                    procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
                    mainProcurementCategory = tender.mainProcurementCategory,
                    eligibilityCriteria = tender.eligibilityCriteria,
                    contractPeriod = tender.contractPeriod
                        ?.let { tenderPeriod ->
                            PnCreateResponse.Tender.ContractPeriod(
                                startDate = tenderPeriod.startDate,
                                endDate = tenderPeriod.endDate
                            )
                        },
                    procuringEntity = tender.procuringEntity
                        ?.let { procuringEntity ->
                            PnCreateResponse.Tender.ProcuringEntity(
                                id = procuringEntity.id,
                                name = procuringEntity.name,
                                identifier = procuringEntity.identifier
                                    .let { identifier ->
                                        PnCreateResponse.Tender.ProcuringEntity.Identifier(
                                            scheme = identifier.scheme,
                                            id = identifier.id,
                                            legalName = identifier.legalName,
                                            uri = identifier.uri
                                        )
                                    },
                                additionalIdentifiers = procuringEntity.additionalIdentifiers
                                    .map { additionalIdentifier ->
                                        PnCreateResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                            scheme = additionalIdentifier.scheme,
                                            id = additionalIdentifier.id,
                                            legalName = additionalIdentifier.legalName,
                                            uri = additionalIdentifier.uri
                                        )
                                    },
                                address = procuringEntity.address
                                    .let { address ->
                                        PnCreateResponse.Tender.ProcuringEntity.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails
                                                .let { addressDetails ->
                                                    PnCreateResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                                        country = addressDetails.country
                                                            .let { country ->
                                                                PnCreateResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                                    scheme = country.scheme,
                                                                    id = country.id,
                                                                    description = country.description,
                                                                    uri = country.uri
                                                                )
                                                            },
                                                        region = addressDetails.region
                                                            .let { region ->
                                                                PnCreateResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                                    scheme = region.scheme,
                                                                    id = region.id,
                                                                    description = region.description,
                                                                    uri = region.uri
                                                                )
                                                            },
                                                        locality = addressDetails.locality
                                                            .let { locality ->
                                                                PnCreateResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                contactPoint = procuringEntity.contactPoint
                                    .let { contactPoint ->
                                        PnCreateResponse.Tender.ProcuringEntity.ContactPoint(
                                            name = contactPoint.name,
                                            email = contactPoint.email,
                                            telephone = contactPoint.telephone,
                                            faxNumber = contactPoint.faxNumber,
                                            url = contactPoint.url
                                        )
                                    }
                            )
                        },
                    value = tender.value,
                    lotGroups = tender.lotGroups
                        .map { lotGroup ->
                            PnCreateResponse.Tender.LotGroup(
                                optionToCombine = lotGroup.optionToCombine
                            )
                        },
                    lots = tender.lots
                        .map { lot ->
                            PnCreateResponse.Tender.Lot(
                                id = lot.id,
                                internalId = lot.internalId,
                                title = lot.title,
                                description = lot.description,
                                status = lot.status,
                                statusDetails = lot.statusDetails,
                                value = lot.value,
                                options = lot.options
                                    .map { option ->
                                        PnCreateResponse.Tender.Lot.Option(
                                            hasOptions = option.hasOptions
                                        )
                                    },
                                variants = lot.variants
                                    .map { variant ->
                                        PnCreateResponse.Tender.Lot.Variant(
                                            hasVariants = variant.hasVariants
                                        )
                                    },
                                renewals = lot.renewals
                                    .map { renewal ->
                                        PnCreateResponse.Tender.Lot.Renewal(
                                            hasRenewals = renewal.hasRenewals
                                        )
                                    },
                                contractPeriod = lot.contractPeriod
                                    .let { contractPeriod ->
                                        PnCreateResponse.Tender.Lot.ContractPeriod(
                                            startDate = contractPeriod.startDate,
                                            endDate = contractPeriod.endDate
                                        )
                                    },
                                placeOfPerformance = lot.placeOfPerformance
                                    .let { placeOfPerformance ->
                                        PnCreateResponse.Tender.Lot.PlaceOfPerformance(
                                            address = placeOfPerformance.address
                                                .let { address ->
                                                    PnCreateResponse.Tender.Lot.PlaceOfPerformance.Address(
                                                        streetAddress = address.streetAddress,
                                                        postalCode = address.postalCode,
                                                        addressDetails = address.addressDetails
                                                            .let { addressDetails ->
                                                                PnCreateResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                                    country = addressDetails.country
                                                                        .let { country ->
                                                                            PnCreateResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                                scheme = country.scheme,
                                                                                id = country.id,
                                                                                description = country.description,
                                                                                uri = country.uri
                                                                            )
                                                                        },
                                                                    region = addressDetails.region
                                                                        .let { region ->
                                                                            PnCreateResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                                scheme = region.scheme,
                                                                                id = region.id,
                                                                                description = region.description,
                                                                                uri = region.uri
                                                                            )
                                                                        },
                                                                    locality = addressDetails.locality
                                                                        .let { locality ->
                                                                            PnCreateResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                                    },
                                recurrentProcurement = lot.recurrentProcurement
                                    .map { recurrentProcurement ->
                                        PnCreateResponse.Tender.Lot.RecurrentProcurement(
                                            isRecurrent = recurrentProcurement.isRecurrent
                                        )
                                    }
                            )
                        },
                    items = tender.items
                        .map { item ->
                            PnCreateResponse.Tender.Item(
                                id = item.id,
                                internalId = item.internalId,
                                classification = item.classification
                                    .let { classification ->
                                        PnCreateResponse.Tender.Item.Classification(
                                            scheme = classification.scheme,
                                            id = classification.id,
                                            description = classification.description
                                        )
                                    },
                                additionalClassifications = item.additionalClassifications
                                    .map { additionalClassification ->
                                        PnCreateResponse.Tender.Item.AdditionalClassification(
                                            scheme = additionalClassification.scheme,
                                            id = additionalClassification.id,
                                            description = additionalClassification.description
                                        )
                                    },
                                quantity = item.quantity,
                                unit = item.unit
                                    .let { unit ->
                                        PnCreateResponse.Tender.Item.Unit(
                                            id = unit.id,
                                            name = unit.name
                                        )
                                    },
                                description = item.description,
                                relatedLot = item.relatedLot
                            )
                        },
                    requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                    submissionMethod = tender.submissionMethod.toList(),
                    submissionMethodRationale = tender.submissionMethodRationale.toList(),
                    submissionMethodDetails = tender.submissionMethodDetails,
                    documents = tender.documents
                        .map { document ->
                            PnCreateResponse.Tender.Document(
                                documentType = document.documentType,
                                id = document.id,
                                title = document.title,
                                description = document.description,
                                relatedLots = document.relatedLots.toList()
                            )
                        }
                )
            }
    )
