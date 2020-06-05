package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.cn.update.UpdatedCn
import com.procurement.access.application.service.cn.update.UpdatedSelectiveCn
import com.procurement.access.infrastructure.dto.cn.update.UpdateCnResponse
import com.procurement.access.infrastructure.dto.cn.update.UpdateSelectiveCnResponse

fun UpdatedCn.convert(): UpdateCnResponse =
    UpdateCnResponse(
        lotsChanged = this.lotsChanged,
        planning = this.planning.let { planning ->
            UpdateCnResponse.Planning(
                rationale = planning.rationale,
                budget = planning.budget.let { budget ->
                    UpdateCnResponse.Planning.Budget(
                        description = budget.description,
                        amount = budget.amount,
                        isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                        budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                            UpdateCnResponse.Planning.Budget.BudgetBreakdown(
                                id = budgetBreakdown.id,
                                description = budgetBreakdown.description,
                                amount = budgetBreakdown.amount,
                                period = budgetBreakdown.period.let { period ->
                                    UpdateCnResponse.Planning.Budget.BudgetBreakdown.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                    UpdateCnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                        id = sourceParty.id,
                                        name = sourceParty.name
                                    )
                                },
                                europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                    UpdateCnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
        tender = this.tender.let { tender ->
            UpdateCnResponse.Tender(
                id = tender.id,
                status = tender.status,
                statusDetails = tender.statusDetails,
                title = tender.title,
                description = tender.description,
                classification = tender.classification.let { classification ->
                    UpdateCnResponse.Tender.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                    UpdateCnResponse.Tender.TenderPeriod(
                        startDate = tenderPeriod.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                enquiryPeriod = tender.enquiryPeriod
                    ?.let { enquiryPeriod ->
                        UpdateCnResponse.Tender.EnquiryPeriod(
                            startDate = enquiryPeriod.startDate,
                            endDate = enquiryPeriod.endDate
                        )
                    },
                acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                    UpdateCnResponse.Tender.AcceleratedProcedure(
                        isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                    )
                },
                designContest = tender.designContest.let { designContest ->
                    UpdateCnResponse.Tender.DesignContest(
                        serviceContractAward = designContest.serviceContractAward
                    )
                },
                electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                    UpdateCnResponse.Tender.ElectronicWorkflows(
                        useOrdering = electronicWorkflows.useOrdering,
                        usePayment = electronicWorkflows.usePayment,
                        acceptInvoicing = electronicWorkflows.acceptInvoicing
                    )
                },
                jointProcurement = tender.jointProcurement.let { jointProcurement ->
                    UpdateCnResponse.Tender.JointProcurement(
                        isJointProcurement = jointProcurement.isJointProcurement
                    )
                },
                procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                    UpdateCnResponse.Tender.ProcedureOutsourcing(
                        procedureOutsourced = procedureOutsourcing.procedureOutsourced
                    )
                },
                framework = tender.framework.let { framework ->
                    UpdateCnResponse.Tender.Framework(
                        isAFramework = framework.isAFramework
                    )
                },
                dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                    UpdateCnResponse.Tender.DynamicPurchasingSystem(
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
                contractPeriod = tender.contractPeriod.let { tenderPeriod ->
                    UpdateCnResponse.Tender.ContractPeriod(
                        startDate = tenderPeriod.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                procurementMethodModalities = tender.procurementMethodModalities,
                electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                    UpdateCnResponse.Tender.ElectronicAuctions(
                        details = electronicAuctions.details.map { detail ->
                            UpdateCnResponse.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = detail.relatedLot,
                                electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                    UpdateCnResponse.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                        eligibleMinimumDifference = modality.eligibleMinimumDifference
                                    )
                                }
                            )
                        }
                    )
                },
                procuringEntity = tender.procuringEntity.let { procuringEntity ->
                    UpdateCnResponse.Tender.ProcuringEntity(
                        id = procuringEntity.id,
                        name = procuringEntity.name,
                        identifier = procuringEntity.identifier.let { identifier ->
                            UpdateCnResponse.Tender.ProcuringEntity.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                        additionalIdentifiers = procuringEntity.additionalIdentifiers.map { additionalIdentifier ->
                            UpdateCnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                        address = procuringEntity.address.let { address ->
                            UpdateCnResponse.Tender.ProcuringEntity.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    UpdateCnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            UpdateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            UpdateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            UpdateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                        persons = procuringEntity.persons.map { person ->
                            UpdateCnResponse.Tender.ProcuringEntity.Person(
                                id = person.id,
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier.let { identifier ->
                                    UpdateCnResponse.Tender.ProcuringEntity.Person.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                                businessFunctions = person.businessFunctions.map { businessFunction ->
                                    UpdateCnResponse.Tender.ProcuringEntity.Person.BusinessFunction(
                                        id = businessFunction.id,
                                        type = businessFunction.type,
                                        jobTitle = businessFunction.jobTitle,
                                        period = businessFunction.period.let { period ->
                                            UpdateCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                startDate = period.startDate
                                            )
                                        },
                                        documents = businessFunction.documents.map { document ->
                                            UpdateCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                                                id = document.id,
                                                documentType = document.documentType,
                                                title = document.title,
                                                description = document.description
                                            )
                                        }
                                    )
                                }
                            )
                        },
                        contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                            UpdateCnResponse.Tender.ProcuringEntity.ContactPoint(
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
                lotGroups = tender.lotGroups.map { lotGroup ->
                    UpdateCnResponse.Tender.LotGroup(
                        optionToCombine = lotGroup.optionToCombine
                    )
                },
                lots = tender.lots.map { lot ->
                    UpdateCnResponse.Tender.Lot(
                        id = lot.id,
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        status = lot.status,
                        statusDetails = lot.statusDetails,
                        value = lot.value,
                        options = lot.options.map { option ->
                            UpdateCnResponse.Tender.Lot.Option(
                                hasOptions = option.hasOptions
                            )
                        },
                        variants = lot.variants.map { variant ->
                            UpdateCnResponse.Tender.Lot.Variant(
                                hasVariants = variant.hasVariants
                            )
                        },
                        renewals = lot.renewals.map { renewal ->
                            UpdateCnResponse.Tender.Lot.Renewal(
                                hasRenewals = renewal.hasRenewals
                            )
                        },
                        recurrentProcurements = lot.recurrentProcurements.map { recurrentProcurement ->
                            UpdateCnResponse.Tender.Lot.RecurrentProcurement(
                                isRecurrent = recurrentProcurement.isRecurrent
                            )
                        },
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdateCnResponse.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdateCnResponse.Tender.Lot.PlaceOfPerformance(
                                address = placeOfPerformance.address.let { address ->
                                    UpdateCnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                },
                items = tender.items.map { item ->
                    UpdateCnResponse.Tender.Item(
                        id = item.id,
                        internalId = item.internalId,
                        classification = item.classification.let { classification ->
                            UpdateCnResponse.Tender.Item.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                        additionalClassifications = item.additionalClassifications.map { additionalClassification ->
                            UpdateCnResponse.Tender.Item.AdditionalClassification(
                                scheme = additionalClassification.scheme,
                                id = additionalClassification.id,
                                description = additionalClassification.description
                            )
                        },
                        quantity = item.quantity,
                        unit = item.unit.let { unit ->
                            UpdateCnResponse.Tender.Item.Unit(
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
                documents = tender.documents.map { document ->
                    UpdateCnResponse.Tender.Document(
                        documentType = document.documentType,
                        id = document.id,
                        title = document.title,
                        description = document.description,
                        relatedLots = document.relatedLots.toList()
                    )
                },
                otherCriteria = tender.otherCriteria?.let { otherCriteria ->
                    UpdateCnResponse.Tender.OtherCriteria(
                        reductionCriteria = otherCriteria.reductionCriteria,
                        qualificationSystemMethods = otherCriteria.qualificationSystemMethods
                    )
                }
            )
        },
        amendment = amendment?.let { amendment ->
            UpdateCnResponse.Amendment(
                relatedLots = amendment.relatedLots.toList()
            )
        }
    )

fun UpdatedSelectiveCn.convert(): UpdateSelectiveCnResponse =
    UpdateSelectiveCnResponse(
        lotsChanged = this.lotsChanged,
        planning = this.planning.let { planning ->
            UpdateSelectiveCnResponse.Planning(
                rationale = planning.rationale,
                budget = planning.budget
                    .let { budget ->
                        UpdateSelectiveCnResponse.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount,
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                            budgetBreakdowns = budget.budgetBreakdowns
                                .map { budgetBreakdown ->
                                    UpdateSelectiveCnResponse.Planning.Budget.BudgetBreakdown(
                                        id = budgetBreakdown.id,
                                        description = budgetBreakdown.description,
                                        amount = budgetBreakdown.amount,
                                        period = budgetBreakdown.period
                                            .let { period ->
                                                UpdateSelectiveCnResponse.Planning.Budget.BudgetBreakdown.Period(
                                                    startDate = period.startDate,
                                                    endDate = period.endDate
                                                )
                                            },
                                        sourceParty = budgetBreakdown.sourceParty
                                            .let { sourceParty ->
                                                UpdateSelectiveCnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                                    id = sourceParty.id,
                                                    name = sourceParty.name
                                                )
                                            },
                                        europeanUnionFunding = budgetBreakdown.europeanUnionFunding
                                            ?.let { europeanUnionFunding ->
                                                UpdateSelectiveCnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
                UpdateSelectiveCnResponse.Tender(
                    id = tender.id,
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    title = tender.title,
                    description = tender.description,
                    classification = tender.classification
                        .let { classification ->
                            UpdateSelectiveCnResponse.Tender.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                    acceleratedProcedure = tender.acceleratedProcedure
                        .let { acceleratedProcedure ->
                            UpdateSelectiveCnResponse.Tender.AcceleratedProcedure(
                                isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                            )
                        },
                    designContest = tender.designContest
                        .let { designContest ->
                            UpdateSelectiveCnResponse.Tender.DesignContest(
                                serviceContractAward = designContest.serviceContractAward
                            )
                        },
                    electronicWorkflows = tender.electronicWorkflows
                        .let { electronicWorkflows ->
                            UpdateSelectiveCnResponse.Tender.ElectronicWorkflows(
                                useOrdering = electronicWorkflows.useOrdering,
                                usePayment = electronicWorkflows.usePayment,
                                acceptInvoicing = electronicWorkflows.acceptInvoicing
                            )
                        },
                    jointProcurement = tender.jointProcurement
                        .let { jointProcurement ->
                            UpdateSelectiveCnResponse.Tender.JointProcurement(
                                isJointProcurement = jointProcurement.isJointProcurement
                            )
                        },
                    procedureOutsourcing = tender.procedureOutsourcing
                        .let { procedureOutsourcing ->
                            UpdateSelectiveCnResponse.Tender.ProcedureOutsourcing(
                                procedureOutsourced = procedureOutsourcing.procedureOutsourced
                            )
                        },
                    framework = tender.framework
                        .let { framework ->
                            UpdateSelectiveCnResponse.Tender.Framework(
                                isAFramework = framework.isAFramework
                            )
                        },
                    dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                        .let { dynamicPurchasingSystem ->
                            UpdateSelectiveCnResponse.Tender.DynamicPurchasingSystem(
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
                        .let { tenderPeriod ->
                            UpdateSelectiveCnResponse.Tender.ContractPeriod(
                                startDate = tenderPeriod.startDate,
                                endDate = tenderPeriod.endDate
                            )
                        },
                    procurementMethodModalities = tender.procurementMethodModalities,
                    electronicAuctions = tender.electronicAuctions
                        ?.let { electronicAuctions ->
                            UpdateSelectiveCnResponse.Tender.ElectronicAuctions(
                                details = electronicAuctions.details
                                    .map { detail ->
                                        UpdateSelectiveCnResponse.Tender.ElectronicAuctions.Detail(
                                            id = detail.id,
                                            relatedLot = detail.relatedLot,
                                            electronicAuctionModalities = detail.electronicAuctionModalities
                                                .map { modality ->
                                                    UpdateSelectiveCnResponse.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                                        eligibleMinimumDifference = modality.eligibleMinimumDifference
                                                    )
                                                }
                                        )
                                    }
                            )
                        },
                    procuringEntity = tender.procuringEntity
                        .let { procuringEntity ->
                            UpdateSelectiveCnResponse.Tender.ProcuringEntity(
                                id = procuringEntity.id,
                                name = procuringEntity.name,
                                identifier = procuringEntity.identifier
                                    .let { identifier ->
                                        UpdateSelectiveCnResponse.Tender.ProcuringEntity.Identifier(
                                            scheme = identifier.scheme,
                                            id = identifier.id,
                                            legalName = identifier.legalName,
                                            uri = identifier.uri
                                        )
                                    },
                                additionalIdentifiers = procuringEntity.additionalIdentifiers
                                    .map { additionalIdentifier ->
                                        UpdateSelectiveCnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                            scheme = additionalIdentifier.scheme,
                                            id = additionalIdentifier.id,
                                            legalName = additionalIdentifier.legalName,
                                            uri = additionalIdentifier.uri
                                        )
                                    },
                                address = procuringEntity.address
                                    .let { address ->
                                        UpdateSelectiveCnResponse.Tender.ProcuringEntity.Address(
                                            streetAddress = address.streetAddress,
                                            postalCode = address.postalCode,
                                            addressDetails = address.addressDetails
                                                .let { addressDetails ->
                                                    UpdateSelectiveCnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                                        country = addressDetails.country
                                                            .let { country ->
                                                                UpdateSelectiveCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                                    scheme = country.scheme,
                                                                    id = country.id,
                                                                    description = country.description,
                                                                    uri = country.uri
                                                                )
                                                            },
                                                        region = addressDetails.region
                                                            .let { region ->
                                                                UpdateSelectiveCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                                    scheme = region.scheme,
                                                                    id = region.id,
                                                                    description = region.description,
                                                                    uri = region.uri
                                                                )
                                                            },
                                                        locality = addressDetails.locality
                                                            .let { locality ->
                                                                UpdateSelectiveCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                                persons = procuringEntity.persons
                                    .map { person ->
                                        UpdateSelectiveCnResponse.Tender.ProcuringEntity.Person(
                                            id = person.id,
                                            title = person.title,
                                            name = person.name,
                                            identifier = person.identifier
                                                .let { identifier ->
                                                    UpdateSelectiveCnResponse.Tender.ProcuringEntity.Person.Identifier(
                                                        scheme = identifier.scheme,
                                                        id = identifier.id,
                                                        uri = identifier.uri
                                                    )
                                                },
                                            businessFunctions = person.businessFunctions
                                                .map { businessFunction ->
                                                    UpdateSelectiveCnResponse.Tender.ProcuringEntity.Person.BusinessFunction(
                                                        id = businessFunction.id,
                                                        type = businessFunction.type,
                                                        jobTitle = businessFunction.jobTitle,
                                                        period = businessFunction.period
                                                            .let { period ->
                                                                UpdateSelectiveCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                                    startDate = period.startDate
                                                                )
                                                            },
                                                        documents = businessFunction.documents
                                                            .map { document ->
                                                                UpdateSelectiveCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
                                                                    id = document.id,
                                                                    documentType = document.documentType,
                                                                    title = document.title,
                                                                    description = document.description
                                                                )
                                                            }
                                                    )
                                                }
                                        )
                                    },
                                contactPoint = procuringEntity.contactPoint
                                    .let { contactPoint ->
                                        UpdateSelectiveCnResponse.Tender.ProcuringEntity.ContactPoint(
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
                            UpdateSelectiveCnResponse.Tender.LotGroup(
                                optionToCombine = lotGroup.optionToCombine
                            )
                        },
                    lots = tender.lots
                        .map { lot ->
                            UpdateSelectiveCnResponse.Tender.Lot(
                                id = lot.id,
                                internalId = lot.internalId,
                                title = lot.title,
                                description = lot.description,
                                status = lot.status,
                                statusDetails = lot.statusDetails,
                                value = lot.value,
                                options = lot.options
                                    .map { option ->
                                        UpdateSelectiveCnResponse.Tender.Lot.Option(
                                            hasOptions = option.hasOptions
                                        )
                                    },
                                variants = lot.variants
                                    .map { variant ->
                                        UpdateSelectiveCnResponse.Tender.Lot.Variant(
                                            hasVariants = variant.hasVariants
                                        )
                                    },
                                renewals = lot.renewals
                                    .map { renewal ->
                                        UpdateSelectiveCnResponse.Tender.Lot.Renewal(
                                            hasRenewals = renewal.hasRenewals
                                        )
                                    },
                                recurrentProcurements = lot.recurrentProcurements
                                    .map { recurrentProcurement ->
                                        UpdateSelectiveCnResponse.Tender.Lot.RecurrentProcurement(
                                            isRecurrent = recurrentProcurement.isRecurrent
                                        )
                                    },
                                contractPeriod = lot.contractPeriod
                                    .let { contractPeriod ->
                                        UpdateSelectiveCnResponse.Tender.Lot.ContractPeriod(
                                            startDate = contractPeriod.startDate,
                                            endDate = contractPeriod.endDate
                                        )
                                    },
                                placeOfPerformance = lot.placeOfPerformance
                                    .let { placeOfPerformance ->
                                        UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance(
                                            address = placeOfPerformance.address
                                                .let { address ->
                                                    UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                                        streetAddress = address.streetAddress,
                                                        postalCode = address.postalCode,
                                                        addressDetails = address.addressDetails
                                                            .let { addressDetails ->
                                                                UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                                    country = addressDetails.country
                                                                        .let { country ->
                                                                            UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                                scheme = country.scheme,
                                                                                id = country.id,
                                                                                description = country.description,
                                                                                uri = country.uri
                                                                            )
                                                                        },
                                                                    region = addressDetails.region
                                                                        .let { region ->
                                                                            UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                                scheme = region.scheme,
                                                                                id = region.id,
                                                                                description = region.description,
                                                                                uri = region.uri
                                                                            )
                                                                        },
                                                                    locality = addressDetails.locality
                                                                        .let { locality ->
                                                                            UpdateSelectiveCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                        },
                    items = tender.items
                        .map { item ->
                            UpdateSelectiveCnResponse.Tender.Item(
                                id = item.id,
                                internalId = item.internalId,
                                classification = item.classification
                                    .let { classification ->
                                        UpdateSelectiveCnResponse.Tender.Item.Classification(
                                            scheme = classification.scheme,
                                            id = classification.id,
                                            description = classification.description
                                        )
                                    },
                                additionalClassifications = item.additionalClassifications
                                    .map { additionalClassification ->
                                        UpdateSelectiveCnResponse.Tender.Item.AdditionalClassification(
                                            scheme = additionalClassification.scheme,
                                            id = additionalClassification.id,
                                            description = additionalClassification.description
                                        )
                                    },
                                quantity = item.quantity,
                                unit = item.unit
                                    .let { unit ->
                                        UpdateSelectiveCnResponse.Tender.Item.Unit(
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
                            UpdateSelectiveCnResponse.Tender.Document(
                                documentType = document.documentType,
                                id = document.id,
                                title = document.title,
                                description = document.description,
                                relatedLots = document.relatedLots.toList()
                            )
                        },
                    secondStage = tender.secondStage
                        ?.let { otherCriteria ->
                            UpdateSelectiveCnResponse.Tender.SecondStage(
                                minimumCandidates = otherCriteria.minimumCandidates,
                                maximumCandidates = otherCriteria.maximumCandidates
                            )
                        },
                    otherCriteria = tender.otherCriteria
                        .let { otherCriteria ->
                            UpdateSelectiveCnResponse.Tender.OtherCriteria(
                                reductionCriteria = otherCriteria.reductionCriteria,
                                qualificationSystemMethods = otherCriteria.qualificationSystemMethods
                            )
                        }
                )
            },
        amendment = amendment
            ?.let { amendment ->
                UpdateSelectiveCnResponse.Amendment(
                    relatedLots = amendment.relatedLots.toList()
                )
            }
    )