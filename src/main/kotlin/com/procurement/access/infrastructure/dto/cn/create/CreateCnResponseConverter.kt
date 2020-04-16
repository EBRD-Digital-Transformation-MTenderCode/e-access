package com.procurement.access.infrastructure.dto.cn.create

import com.procurement.access.model.dto.ocds.TenderProcess
import java.util.*

fun TenderProcess.convert(token: UUID): CreateCnResponse = CreateCnResponse(
    token = token,
    ocid = this.ocid!!,
    amendment = this.amendment
        ?.let { amendment ->
            CreateCnResponse.Amendment(
                relatedLots = amendment.relatedLots.toList()
            )
        },
    planning = this.planning
        .let { planning ->
            CreateCnResponse.Planning(
                rationale = planning.rationale,
                budget = planning.budget
                    .let { budget ->
                        CreateCnResponse.Planning.Budget(
                            description = budget.description,
                            amount = budget.amount
                                .let { value ->
                                    CreateCnResponse.Planning.Budget.Amount(
                                        amount = value.amount,
                                        currency = value.currency
                                    )
                                },
                            isEuropeanUnionFunded = budget.isEuropeanUnionFunded!!,
                            budgetBreakdowns = budget.budgetBreakdown
                                .map { budgetBreakdown ->
                                    CreateCnResponse.Planning.Budget.BudgetBreakdown(
                                        id = budgetBreakdown.id,
                                        amount = budgetBreakdown.amount
                                            .let { value ->
                                                CreateCnResponse.Planning.Budget.BudgetBreakdown.Amount(
                                                    amount = value.amount,
                                                    currency = value.currency
                                                )
                                            },
                                        description = budgetBreakdown.description,
                                        period = budgetBreakdown.period
                                            .let { period ->
                                                CreateCnResponse.Planning.Budget.BudgetBreakdown.Period(
                                                    startDate = period.startDate,
                                                    endDate = period.endDate!!
                                                )
                                            },
                                        sourceParty = budgetBreakdown.sourceParty!!
                                            .let { sourceParty ->
                                                CreateCnResponse.Planning.Budget.BudgetBreakdown.SourceParty(
                                                    id = sourceParty.id,
                                                    name = sourceParty.name
                                                )
                                            },
                                        europeanUnionFunding = budgetBreakdown.europeanUnionFunding
                                            ?.let { europeanUnionFunding ->
                                                CreateCnResponse.Planning.Budget.BudgetBreakdown.EuropeanUnionFunding(
                                                    projectName = europeanUnionFunding.projectName,
                                                    uri = europeanUnionFunding.uri,
                                                    projectIdentifier = europeanUnionFunding.projectIdentifier
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
            CreateCnResponse.Tender(
                id = tender.id!!,
                description = tender.description,
                status = tender.status,
                statusDetails = tender.statusDetails,
                title = tender.title,
                procuringEntity = tender.procuringEntity
                    .let { organizationReference ->
                        CreateCnResponse.Tender.ProcuringEntity(
                            id = organizationReference.id!!,
                            name = organizationReference.name,
                            persones = null,
                            identifier = organizationReference.identifier
                                .let { identifier ->
                                    CreateCnResponse.Tender.ProcuringEntity.Identifier(
                                        id = identifier.id,
                                        uri = identifier.uri,
                                        legalName = identifier.legalName,
                                        scheme = identifier.scheme
                                    )
                                },
                            address = organizationReference.address!!
                                .let { address ->
                                    CreateCnResponse.Tender.ProcuringEntity.Address(
                                        streetAddress = address.streetAddress,
                                        addressDetails = address.addressDetails
                                            .let { addressDetails ->
                                                CreateCnResponse.Tender.ProcuringEntity.Address.AddressDetails(
                                                    country = addressDetails.country
                                                        .let { countryDetails ->
                                                            CreateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                                id = countryDetails.id,
                                                                scheme = countryDetails.scheme!!,
                                                                uri = countryDetails.uri!!,
                                                                description = countryDetails.description!!
                                                            )
                                                        },
                                                    region = addressDetails.region
                                                        .let { regionDetails ->
                                                            CreateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                                id = regionDetails.id,
                                                                description = regionDetails.scheme!!,
                                                                uri = regionDetails.uri!!,
                                                                scheme = regionDetails.scheme!!
                                                            )
                                                        },
                                                    locality = addressDetails.locality
                                                        .let { localityDetails ->
                                                            CreateCnResponse.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                                                                id = localityDetails.id,
                                                                scheme = localityDetails.scheme,
                                                                description = localityDetails.description,
                                                                uri = localityDetails.uri
                                                            )
                                                        }
                                                )
                                            },
                                        postalCode = address.postalCode
                                    )
                                },
                            contactPoint = organizationReference.contactPoint!!
                                .let { contactPoint ->
                                    CreateCnResponse.Tender.ProcuringEntity.ContactPoint(
                                        name = contactPoint.name,
                                        faxNumber = contactPoint.faxNumber,
                                        telephone = contactPoint.telephone,
                                        email = contactPoint.email,
                                        url = contactPoint.url
                                    )
                                },
                            additionalIdentifiers = organizationReference.additionalIdentifiers
                                ?.map { identifier ->
                                    CreateCnResponse.Tender.ProcuringEntity.AdditionalIdentifier(
                                        id = identifier.id,
                                        legalName = identifier.legalName,
                                        scheme = identifier.scheme,
                                        uri = identifier.uri
                                    )
                                }
                        )
                    },
                documents = tender.documents!!
                    .map { document ->
                        CreateCnResponse.Tender.Document(
                            id = document.id,
                            description = document.description,
                            title = document.title,
                            relatedLots = document.relatedLots?.toList(),
                            documentType = document.documentType
                        )
                    },
                value = tender.value
                    .let { value ->
                        CreateCnResponse.Tender.Value(
                            amount = value.amount,
                            currency = value.currency
                        )
                    },
                procedureOutsourcing = tender.procedureOutsourcing
                    .let { procedureOutsourcing ->
                        CreateCnResponse.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced!!
                        )
                    },
                jointProcurement = tender.jointProcurement
                    .let { jointProcurement ->
                        CreateCnResponse.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement!!
                        )
                    },
                framework = tender.framework
                    .let { framework ->
                        CreateCnResponse.Tender.Framework(
                            isAFramework = framework.isAFramework!!
                        )
                    },
                electronicWorkflows = tender.electronicWorkflows
                    .let { electronicWorkflows ->
                        CreateCnResponse.Tender.ElectronicWorkflows(
                            usePayment = electronicWorkflows.usePayment!!,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing!!,
                            useOrdering = electronicWorkflows.useOrdering!!
                        )
                    },
                dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                    .let { dynamicPurchasingSystem ->
                        CreateCnResponse.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem!!
                        )
                    },
                designContest = tender.designContest
                    .let { designContest ->
                        CreateCnResponse.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward!!
                        )
                    },
                acceleratedProcedure = tender.acceleratedProcedure
                    .let { acceleratedProcedure ->
                        CreateCnResponse.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure!!
                        )
                    },
                procurementMethodAdditionalInfo = tender.procurementMethodAdditionalInfo,
                procurementMethodRationale = tender.procurementMethodRationale,
                procurementMethod = tender.procurementMethod,
                eligibilityCriteria = tender.eligibilityCriteria,
                mainProcurementCategory = tender.mainProcurementCategory,
                legalBasis = tender.legalBasis,
                procurementMethodDetails = tender.procurementMethodDetails,
                contractPeriod = tender.contractPeriod
                    ?.let { contractPeriod ->
                        CreateCnResponse.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                classification = tender.classification
                    .let { classification ->
                        CreateCnResponse.Tender.Classification(
                            id = classification.id,
                            description = classification.description,
                            scheme = classification.scheme,
                            uri = classification.uri
                        )
                    },
                items = tender.items
                    .map { item ->
                        CreateCnResponse.Tender.Item(
                            id = item.id!!,
                            description = item.description!!,
                            classification = item.classification
                                .let { classification ->
                                    CreateCnResponse.Tender.Item.Classification(
                                        id = classification.id,
                                        description = classification.description,
                                        scheme = classification.scheme,
                                        uri = classification.uri
                                    )
                                },
                            relatedLot = item.relatedLot,
                            unit = item.unit
                                .let { unit ->
                                    CreateCnResponse.Tender.Item.Unit(
                                        id = unit.id,
                                        name = unit.name
                                    )
                                },
                            additionalClassifications = item.additionalClassifications
                                ?.map { classification ->
                                    CreateCnResponse.Tender.Item.AdditionalClassification(
                                        id = classification.id,
                                        scheme = classification.scheme,
                                        description = classification.description
                                    )
                                },
                            quantity = item.quantity,
                            internalId = item.internalId
                        )
                    },
                tenderPeriod = tender.tenderPeriod
                    ?.let { period ->
                        CreateCnResponse.Tender.TenderPeriod(
                            startDate = period.startDate,
                            endDate = period.endDate!!
                        )
                    },
                submissionMethod = tender.submissionMethod,
                requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                procurementMethodModalities = tender.procurementMethodModalities,
                lots = tender.lots
                    .map { lot ->
                        CreateCnResponse.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            description = lot.description!!,
                            contractPeriod = lot.contractPeriod!!
                                .let { contractPeriod ->
                                    CreateCnResponse.Tender.Lot.ContractPeriod(
                                        startDate = contractPeriod.startDate,
                                        endDate = contractPeriod.endDate
                                    )
                                },
                            value = lot.value
                                .let { value ->
                                    CreateCnResponse.Tender.Lot.Value(
                                        amount = value.amount,
                                        currency = value.currency
                                    )
                                },
                            title = lot.title!!,
                            status = lot.status!!,
                            statusDetails = lot.statusDetails!!,
                            variants = lot.variants!!
                                .map { variant ->
                                    CreateCnResponse.Tender.Lot.Variant(
                                        hasVariants = variant.hasVariants!!
                                    )
                                },
                            renewals = lot.renewals!!
                                .map { renewal ->
                                    CreateCnResponse.Tender.Lot.Renewal(
                                        hasRenewals = renewal.hasRenewals!!
                                    )
                                },
                            placeOfPerformance = lot.placeOfPerformance!!
                                .let { placeOfPerformance ->
                                    CreateCnResponse.Tender.Lot.PlaceOfPerformance(
                                        description = placeOfPerformance.description,
                                        address = placeOfPerformance.address
                                            .let { address ->
                                                CreateCnResponse.Tender.Lot.PlaceOfPerformance.Address(
                                                    streetAddress = address.streetAddress,
                                                    postalCode = address.postalCode,
                                                    addressDetails = address.addressDetails
                                                        .let { addressDetails ->
                                                            CreateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                                country = addressDetails.country
                                                                    .let { countryDetails ->
                                                                        CreateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                            id = countryDetails.id,
                                                                            description = countryDetails.description!!,
                                                                            scheme = countryDetails.scheme!!,
                                                                            uri = countryDetails.uri!!
                                                                        )
                                                                    },
                                                                region = addressDetails.region
                                                                    .let { regionDetails ->
                                                                        CreateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                            id = regionDetails.id,
                                                                            description = regionDetails.description!!,
                                                                            scheme = regionDetails.scheme!!,
                                                                            uri = regionDetails.uri!!
                                                                        )
                                                                    },
                                                                locality = addressDetails.locality
                                                                    .let { localityDetails ->
                                                                        CreateCnResponse.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
                                                                            id = localityDetails.id,
                                                                            description = localityDetails.description,
                                                                            scheme = localityDetails.scheme,
                                                                            uri = localityDetails.uri
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                },
                            options = lot.options!!
                                .map { option ->
                                    CreateCnResponse.Tender.Lot.Option(
                                        hasOptions = option.hasOptions!!
                                    )
                                },
                            recurrentProcurement = lot.recurrentProcurement!!
                                .map { recurrentProcurement ->
                                    CreateCnResponse.Tender.Lot.RecurrentProcurement(
                                        isRecurrent = recurrentProcurement.isRecurrent
                                    )
                                }
                        )
                    },
                lotGroups = tender.lotGroups
                    .map { lotGroup ->
                        CreateCnResponse.Tender.LotGroup(
                            optionToCombine = lotGroup.optionToCombine!!
                        )
                    },
                enquiryPeriod = tender.enquiryPeriod
                    ?.let { period ->
                        CreateCnResponse.Tender.EnquiryPeriod(
                            startDate = period.startDate,
                            endDate = period.endDate!!
                        )
                    },
                electronicAuctions = tender.electronicAuctions
                    ?.let { electronicAuctions ->
                        CreateCnResponse.Tender.ElectronicAuctions(
                            details = electronicAuctions.details
                                .map { electronicAuctionsDetails ->
                                    CreateCnResponse.Tender.ElectronicAuctions.Detail(
                                        id = electronicAuctionsDetails.id,
                                        relatedLot = electronicAuctionsDetails.relatedLot,
                                        electronicAuctionModalities = electronicAuctionsDetails.electronicAuctionModalities
                                            .map { electronicAuctionModalities ->
                                                CreateCnResponse.Tender.ElectronicAuctions.Detail.Modalities(
                                                    eligibleMinimumDifference = electronicAuctionModalities.eligibleMinimumDifference.
                                                        let { modalities ->
                                                            CreateCnResponse.Tender.ElectronicAuctions.Detail.Modalities.EligibleMinimumDifference(
                                                                amount = modalities.amount,
                                                                currency = modalities.currency
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                }
                        )
                    },
                criteria = emptyList(),
                conversions = emptyList(),
                awardCriteriaDetails = null,
                awardCriteria = tender.awardCriteria,
                submissionMethodDetails = tender.submissionMethodDetails,
                submissionMethodRationale = tender.submissionMethodRationale

            )
        }
)
