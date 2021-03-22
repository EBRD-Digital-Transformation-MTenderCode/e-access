package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.handler.v1.model.response.ApUpdateResponse

fun APEntity.convert() = ApUpdateResponse(
    tender = this.tender
        .let { tender ->
            ApUpdateResponse.Tender(
                id = tender.id,
                title = tender.title,
                description = tender.description,
                status = tender.status,
                statusDetails = tender.statusDetails,
                value = tender.value.let {value ->
                    ApUpdateResponse.Tender.Value(amount = value.amount, currency = value.currency)
                },
                mainProcurementCategory = tender.mainProcurementCategory,
                procurementMethodRationale = tender.procurementMethodRationale,
                contractPeriod = tender.contractPeriod
                    !!.let { contractPeriod ->
                        ApUpdateResponse.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                tenderPeriod = tender.tenderPeriod
                    .let { tenderPeriod ->
                        ApUpdateResponse.Tender.TenderPeriod(
                            startDate = tenderPeriod.startDate
                        )
                    },
                documents = tender.documents
                    ?.map { document ->
                        ApUpdateResponse.Tender.Document(
                            documentType = document.documentType,
                            id = document.id,
                            title = document.title,
                            description = document.description,
                            relatedLots = document.relatedLots
                        )
                    },
                classification = tender.classification
                    .let { classification ->
                        ApUpdateResponse.Tender.Classification(
                            scheme = classification.scheme,
                            id = classification.id,
                            description = classification.description
                        )
                    },
                lots = tender.lots
                    ?.map { lot ->
                        ApUpdateResponse.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            title = lot.title,
                            status = lot.status,
                            statusDetails = lot.statusDetails,
                            description = lot.description,
                            placeOfPerformance = lot.placeOfPerformance
                                ?.let { placeOfPerformance ->
                                    ApUpdateResponse.Tender.Lot.PlaceOfPerformance(
                                        address = placeOfPerformance.address
                                            .let { address ->
                                                ApUpdateResponse.Tender.Address(
                                                    streetAddress = address.streetAddress,
                                                    postalCode = address.postalCode,
                                                    addressDetails = address.addressDetails
                                                        .let { addressDetails ->
                                                            ApUpdateResponse.Tender.Address.AddressDetails(
                                                                country = addressDetails.country
                                                                    .let { country ->
                                                                        ApUpdateResponse.Tender.Address.AddressDetails.Country(
                                                                            scheme = country.scheme,
                                                                            id = country.id,
                                                                            description = country.description,
                                                                            uri = country.uri
                                                                        )
                                                                    },
                                                                region = addressDetails.region
                                                                    .let { region ->
                                                                        ApUpdateResponse.Tender.Address.AddressDetails.Region(
                                                                            scheme = region.scheme,
                                                                            id = region.id,
                                                                            description = region.description,
                                                                            uri = region.uri
                                                                        )
                                                                    },
                                                                locality = addressDetails.locality
                                                                    ?.let { locality ->
                                                                        ApUpdateResponse.Tender.Address.AddressDetails.Locality(
                                                                            scheme = locality.scheme,
                                                                            id = locality.id,
                                                                            description = locality.description,
                                                                            uri = locality.uri
                                                                        )
                                                                    }
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                }
                        )
                    },
                items = tender.items
                    ?.map { item ->
                        ApUpdateResponse.Tender.Item(
                            id = item.id,
                            internalId = item.internalId,
                            classification = item.classification
                                .let { classification ->
                                    ApUpdateResponse.Tender.Item.Classification(
                                        scheme = classification.scheme,
                                        id = classification.id,
                                        description = classification.description
                                    )
                                },
                            additionalClassifications = item.additionalClassifications
                                ?.map { additionalClassification ->
                                    ApUpdateResponse.Tender.Item.AdditionalClassification(
                                        scheme = additionalClassification.scheme,
                                        id = additionalClassification.id,
                                        description = additionalClassification.description
                                    )
                                },
                            quantity = item.quantity,
                            unit = item.unit
                                .let { unit ->
                                    ApUpdateResponse.Tender.Item.Unit(
                                        id = unit.id,
                                        name = unit.name
                                    )
                                },
                            description = item.description,
                            relatedLot = item.relatedLot,
                            deliveryAddress = item.deliveryAddress
                                ?.let { address ->
                                    ApUpdateResponse.Tender.Address(
                                        streetAddress = address.streetAddress,
                                        postalCode = address.postalCode,
                                        addressDetails = address.addressDetails
                                            .let { addressDetails ->
                                                ApUpdateResponse.Tender.Address.AddressDetails(
                                                    country = addressDetails.country
                                                        .let { country ->
                                                            ApUpdateResponse.Tender.Address.AddressDetails.Country(
                                                                scheme = country.scheme,
                                                                id = country.id,
                                                                description = country.description,
                                                                uri = country.uri
                                                            )
                                                        },
                                                    region = addressDetails.region
                                                        .let { region ->
                                                            ApUpdateResponse.Tender.Address.AddressDetails.Region(
                                                                scheme = region.scheme,
                                                                id = region.id,
                                                                description = region.description,
                                                                uri = region.uri
                                                            )
                                                        },
                                                    locality = addressDetails.locality
                                                        ?.let { locality ->
                                                            ApUpdateResponse.Tender.Address.AddressDetails.Locality(
                                                                scheme = locality.scheme,
                                                                id = locality.id,
                                                                description = locality.description,
                                                                uri = locality.uri
                                                            )
                                                        }
                                                )
                                            }
                                    )
                                }
                        )
                    },
                acceleratedProcedure = tender.acceleratedProcedure
                    .let { acceleratedProcedure ->
                        ApUpdateResponse.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                designContest = tender.designContest
                    .let { designContest ->
                        ApUpdateResponse.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                electronicWorkflows = tender.electronicWorkflows
                    .let { electronicWorkflows ->
                        ApUpdateResponse.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            usePayment = electronicWorkflows.usePayment,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing
                        )
                    },
                jointProcurement = tender.jointProcurement
                    .let { jointProcurement ->
                        ApUpdateResponse.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                procedureOutsourcing = tender.procedureOutsourcing
                    .let { procedureOutsourcing ->
                        ApUpdateResponse.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                framework = tender.framework
                    .let { framework ->
                        ApUpdateResponse.Tender.Framework(
                            isAFramework = framework.isAFramework
                        )
                    },
                dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                    .let { dynamicPurchasingSystem ->
                        ApUpdateResponse.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                        )
                    },
                legalBasis = tender.legalBasis,
                procurementMethod = tender.procurementMethod,
                procurementMethodDetails = tender.procurementMethodDetails,
                eligibilityCriteria = tender.eligibilityCriteria,
                submissionMethod = tender.submissionMethod,
                submissionMethodRationale = tender.submissionMethodRationale,
                submissionMethodDetails = tender.submissionMethodDetails,
                parties = tender.parties.map { party ->
                    ApUpdateResponse.Tender.Party(
                        id = party.id,
                        name = party.name,
                        identifier = party.identifier.let { identifier ->
                            ApUpdateResponse.Tender.Party.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                        additionalIdentifiers = party.additionalIdentifiers?.map { additionalIdentifier ->
                            ApUpdateResponse.Tender.Party.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                        address = party.address.let { address ->
                            ApUpdateResponse.Tender.Party.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    ApUpdateResponse.Tender.Party.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            ApUpdateResponse.Tender.Party.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            ApUpdateResponse.Tender.Party.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            ApUpdateResponse.Tender.Party.Address.AddressDetails.Locality(
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
                        contactPoint = party.contactPoint.let { contactPoint ->
                            ApUpdateResponse.Tender.Party.ContactPoint(
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
        }
)