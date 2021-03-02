package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.cn.update.UpdatedOpenCn
import com.procurement.access.application.service.cn.update.UpdatedSelectiveCn
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateOpenCnResponse
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateSelectiveCnResponse

fun UpdatedOpenCn.convert(): UpdateOpenCnResponse =
    UpdateOpenCnResponse(
        lotsChanged = this.lotsChanged,
        planning = this.planning.let { planning ->
            UpdateOpenCnResponse.Planning(
                rationale = planning.rationale,
                budget = planning.budget.let { budget ->
                    UpdateOpenCnResponse.Planning.Budget(
                        description = budget.description,
                        amount = budget.amount,
                        isEuropeanUnionFunded = budget.isEuropeanUnionFunded,
                        budgetBreakdowns = budget.budgetBreakdowns.map { budgetBreakdown ->
                            UpdateOpenCnResponse.Planning.Budget.BudgetBreakdown(
                                id = budgetBreakdown.id,
                                description = budgetBreakdown.description,
                                amount = budgetBreakdown.amount,
                                period = budgetBreakdown.period.let { period ->
                                    UpdateOpenCnResponse.Planning.Budget.BudgetBreakdown.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate
                                    )
                                },
                                sourceParty = budgetBreakdown.sourceParty.let { sourceParty ->
                                    UpdateOpenCnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                        id = sourceParty.id,
                                        name = sourceParty.name
                                    )
                                },
                                europeanUnionFunding = budgetBreakdown.europeanUnionFunding?.let { europeanUnionFunding ->
                                    UpdateOpenCnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
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
            UpdateOpenCnResponse.Tender(
                id = tender.id,
                status = tender.status,
                statusDetails = tender.statusDetails,
                title = tender.title,
                description = tender.description,
                classification = tender.classification.let { classification ->
                    UpdateOpenCnResponse.Tender.Classification(
                        scheme = classification.scheme,
                        id = classification.id,
                        description = classification.description
                    )
                },
                tenderPeriod = tender.tenderPeriod.let { tenderPeriod ->
                    UpdateOpenCnResponse.Tender.TenderPeriod(
                        startDate = tenderPeriod.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                enquiryPeriod = tender.enquiryPeriod
                    ?.let { enquiryPeriod ->
                        UpdateOpenCnResponse.Tender.EnquiryPeriod(
                            startDate = enquiryPeriod.startDate,
                            endDate = enquiryPeriod.endDate
                        )
                    },
                acceleratedProcedure = tender.acceleratedProcedure.let { acceleratedProcedure ->
                    UpdateOpenCnResponse.Tender.AcceleratedProcedure(
                        isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                    )
                },
                designContest = tender.designContest.let { designContest ->
                    UpdateOpenCnResponse.Tender.DesignContest(
                        serviceContractAward = designContest.serviceContractAward
                    )
                },
                electronicWorkflows = tender.electronicWorkflows.let { electronicWorkflows ->
                    UpdateOpenCnResponse.Tender.ElectronicWorkflows(
                        useOrdering = electronicWorkflows.useOrdering,
                        usePayment = electronicWorkflows.usePayment,
                        acceptInvoicing = electronicWorkflows.acceptInvoicing
                    )
                },
                jointProcurement = tender.jointProcurement.let { jointProcurement ->
                    UpdateOpenCnResponse.Tender.JointProcurement(
                        isJointProcurement = jointProcurement.isJointProcurement
                    )
                },
                procedureOutsourcing = tender.procedureOutsourcing.let { procedureOutsourcing ->
                    UpdateOpenCnResponse.Tender.ProcedureOutsourcing(
                        procedureOutsourced = procedureOutsourcing.procedureOutsourced
                    )
                },
                framework = tender.framework.let { framework ->
                    UpdateOpenCnResponse.Tender.Framework(
                        isAFramework = framework.isAFramework
                    )
                },
                dynamicPurchasingSystem = tender.dynamicPurchasingSystem.let { dynamicPurchasingSystem ->
                    UpdateOpenCnResponse.Tender.DynamicPurchasingSystem(
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
                    UpdateOpenCnResponse.Tender.ContractPeriod(
                        startDate = tenderPeriod.startDate,
                        endDate = tenderPeriod.endDate
                    )
                },
                procurementMethodModalities = tender.procurementMethodModalities,
                electronicAuctions = tender.electronicAuctions?.let { electronicAuctions ->
                    UpdateOpenCnResponse.Tender.ElectronicAuctions(
                        details = electronicAuctions.details.map { detail ->
                            UpdateOpenCnResponse.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = detail.relatedLot,
                                electronicAuctionModalities = detail.electronicAuctionModalities.map { modality ->
                                    UpdateOpenCnResponse.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                        eligibleMinimumDifference = modality.eligibleMinimumDifference
                                    )
                                }
                            )
                        }
                    )
                },
                procuringEntity = tender.procuringEntity.let { procuringEntity ->
                    UpdateOpenCnResponse.Tender.ProcuringEntity(
                        id = procuringEntity.id,
                        name = procuringEntity.name,
                        identifier = procuringEntity.identifier.let { identifier ->
                            UpdateOpenCnResponse.Tender.ProcuringEntity.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                        additionalIdentifiers = procuringEntity.additionalIdentifiers.map { additionalIdentifier ->
                            UpdateOpenCnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                        address = procuringEntity.address.let { address ->
                            UpdateOpenCnResponse.Tender.ProcuringEntity.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    UpdateOpenCnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            UpdateOpenCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            UpdateOpenCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            UpdateOpenCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
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
                            UpdateOpenCnResponse.Tender.ProcuringEntity.Person(
                                id = person.id,
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier.let { identifier ->
                                    UpdateOpenCnResponse.Tender.ProcuringEntity.Person.Identifier(
                                        scheme = identifier.scheme,
                                        id = identifier.id,
                                        uri = identifier.uri
                                    )
                                },
                                businessFunctions = person.businessFunctions.map { businessFunction ->
                                    UpdateOpenCnResponse.Tender.ProcuringEntity.Person.BusinessFunction(
                                        id = businessFunction.id,
                                        type = businessFunction.type,
                                        jobTitle = businessFunction.jobTitle,
                                        period = businessFunction.period.let { period ->
                                            UpdateOpenCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                startDate = period.startDate
                                            )
                                        },
                                        documents = businessFunction.documents.map { document ->
                                            UpdateOpenCnResponse.Tender.ProcuringEntity.Person.BusinessFunction.Document(
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
                            UpdateOpenCnResponse.Tender.ProcuringEntity.ContactPoint(
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
                    UpdateOpenCnResponse.Tender.LotGroup(
                        optionToCombine = lotGroup.optionToCombine
                    )
                },
                lots = tender.lots.map { lot ->
                    UpdateOpenCnResponse.Tender.Lot(
                        id = lot.id,
                        internalId = lot.internalId,
                        title = lot.title,
                        description = lot.description,
                        status = lot.status,
                        statusDetails = lot.statusDetails,
                        value = lot.value,
                        hasOptions = lot.hasOptions,
                        options = lot.options?.map { option ->
                            UpdateOpenCnResponse.Tender.Lot.Option(
                                description = option.description,
                                period = option.period?.let { period ->
                                    UpdateOpenCnResponse.Tender.Lot.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate,
                                        durationInDays = period.durationInDays,
                                        maxExtentDate = period.maxExtentDate
                                    )
                                }
                            )
                        },
                        hasRenewal = lot.hasRenewal,
                        renewal = lot.renewal?.let { renewal ->
                            UpdateOpenCnResponse.Tender.Lot.Renewal(
                                description = renewal.description,
                                minimumRenewals = renewal.minimumRenewals,
                                maximumRenewals = renewal.maximumRenewals,
                                period = renewal.period?.let { period ->
                                    UpdateOpenCnResponse.Tender.Lot.Period(
                                        startDate = period.startDate,
                                        endDate = period.endDate,
                                        durationInDays = period.durationInDays,
                                        maxExtentDate = period.maxExtentDate
                                    )
                                }
                            )
                        },
                        hasRecurrence = lot.hasRecurrence,
                        recurrence = lot.recurrence?.let { recurrence ->
                            UpdateOpenCnResponse.Tender.Lot.Recurrence(
                                description = recurrence.description,
                                dates = recurrence.dates?.map { date ->
                                    UpdateOpenCnResponse.Tender.Lot.Recurrence.Date(
                                        startDate = date.startDate
                                    )
                                }
                            )
                        },
                        contractPeriod = lot.contractPeriod.let { contractPeriod ->
                            UpdateOpenCnResponse.Tender.Lot.ContractPeriod(
                                startDate = contractPeriod.startDate,
                                endDate = contractPeriod.endDate
                            )
                        },
                        placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                            UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance(
                                address = placeOfPerformance.address.let { address ->
                                    UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails.let { addressDetails ->
                                            UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                country = addressDetails.country.let { country ->
                                                    UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                        scheme = country.scheme,
                                                        id = country.id,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                                region = addressDetails.region.let { region ->
                                                    UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                        scheme = region.scheme,
                                                        id = region.id,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                                locality = addressDetails.locality.let { locality ->
                                                    UpdateOpenCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    UpdateOpenCnResponse.Tender.Item(
                        id = item.id,
                        internalId = item.internalId,
                        classification = item.classification.let { classification ->
                            UpdateOpenCnResponse.Tender.Item.Classification(
                                scheme = classification.scheme,
                                id = classification.id,
                                description = classification.description
                            )
                        },
                        additionalClassifications = item.additionalClassifications.map { additionalClassification ->
                            UpdateOpenCnResponse.Tender.Item.AdditionalClassification(
                                scheme = additionalClassification.scheme,
                                id = additionalClassification.id,
                                description = additionalClassification.description
                            )
                        },
                        quantity = item.quantity,
                        unit = item.unit.let { unit ->
                            UpdateOpenCnResponse.Tender.Item.Unit(
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
                    UpdateOpenCnResponse.Tender.Document(
                        documentType = document.documentType,
                        id = document.id,
                        title = document.title,
                        description = document.description,
                        relatedLots = document.relatedLots.toList()
                    )
                },
                otherCriteria = tender.otherCriteria?.let { otherCriteria ->
                    UpdateOpenCnResponse.Tender.OtherCriteria(
                        reductionCriteria = otherCriteria.reductionCriteria,
                        qualificationSystemMethods = otherCriteria.qualificationSystemMethods
                    )
                }
            )
        },
        amendment = amendment?.let { amendment ->
            UpdateOpenCnResponse.Amendment(
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
                                hasOptions = lot.hasOptions,
                                options = lot.options?.map { option ->
                                    UpdateSelectiveCnResponse.Tender.Lot.Option(
                                        description = option.description,
                                        period = option.period?.let { period ->
                                            UpdateSelectiveCnResponse.Tender.Lot.Period(
                                                startDate = period.startDate,
                                                endDate = period.endDate,
                                                durationInDays = period.durationInDays,
                                                maxExtentDate = period.maxExtentDate
                                            )
                                        }
                                    )
                                },
                                hasRenewal = lot.hasRenewal,
                                renewal = lot.renewal?.let { renewal ->
                                    UpdateSelectiveCnResponse.Tender.Lot.Renewal(
                                        description = renewal.description,
                                        minimumRenewals = renewal.minimumRenewals,
                                        maximumRenewals = renewal.maximumRenewals,
                                        period = renewal.period?.let { period ->
                                            UpdateSelectiveCnResponse.Tender.Lot.Period(
                                                startDate = period.startDate,
                                                endDate = period.endDate,
                                                durationInDays = period.durationInDays,
                                                maxExtentDate = period.maxExtentDate
                                            )
                                        }
                                    )
                                },
                                hasRecurrence = lot.hasRecurrence,
                                recurrence = lot.recurrence?.let { recurrence ->
                                    UpdateSelectiveCnResponse.Tender.Lot.Recurrence(
                                        description = recurrence.description,
                                        dates = recurrence.dates?.map { date ->
                                            UpdateSelectiveCnResponse.Tender.Lot.Recurrence.Date(
                                                startDate = date.startDate
                                            )
                                        }
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