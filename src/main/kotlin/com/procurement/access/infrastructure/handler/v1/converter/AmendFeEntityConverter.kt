package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.update.AmendFEResult
import com.procurement.access.infrastructure.entity.FEEntity

class AmendFeEntityConverter {
    companion object {

        fun fromEntity(entity: FEEntity): AmendFEResult =
            AmendFEResult(tender = fromEntity(entity.tender))

        private fun fromEntity(entity: FEEntity.Tender): AmendFEResult.Tender =
            AmendFEResult.Tender(
                id = entity.id,
                status = entity.status,
                statusDetails = entity.statusDetails,
                title = entity.title,
                description = entity.description,
                classification = convert(entity.classification),
                acceleratedProcedure = convert(entity.acceleratedProcedure),
                designContest = convert(entity.designContest),
                electronicWorkflows = convert(entity.electronicWorkflows),
                jointProcurement = convert(entity.jointProcurement),
                procedureOutsourcing = convert(entity.procedureOutsourcing),
                framework = convert(entity.framework),
                dynamicPurchasingSystem = convert(entity.dynamicPurchasingSystem),
                legalBasis = entity.legalBasis,
                procurementMethod = entity.procurementMethod,
                procurementMethodDetails = entity.procurementMethodDetails,
                procurementMethodRationale = entity.procurementMethodRationale,
                eligibilityCriteria = entity.eligibilityCriteria,
                procuringEntity = convert(entity.procuringEntity!!),
                requiresElectronicCatalogue = entity.requiresElectronicCatalogue,
                submissionMethod = entity.submissionMethod.toList(),
                submissionMethodRationale = entity.submissionMethodRationale.toList(),
                submissionMethodDetails = entity.submissionMethodDetails,
                documents = entity.documents
                    ?.map { convert(it) }
                    .orEmpty(),
                procurementMethodModalities = entity.procurementMethodModalities.orEmpty(),
                mainProcurementCategory = entity.mainProcurementCategory,
                value = entity.value,
                secondStage = entity.secondStage
                    ?.let { convert(it) },
                otherCriteria = convert(entity.otherCriteria!!),
                contractPeriod = convert(entity.contractPeriod),
                criteria = entity.criteria
                    ?.map { convert(it) }
                    .orEmpty(),
                parties = entity.parties.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.Classification): AmendFEResult.Tender.Classification =
            AmendFEResult.Tender.Classification(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.AcceleratedProcedure): AmendFEResult.Tender.AcceleratedProcedure =
            AmendFEResult.Tender.AcceleratedProcedure(
                isAcceleratedProcedure = entity.isAcceleratedProcedure
            )

        private fun convert(entity: FEEntity.Tender.DesignContest): AmendFEResult.Tender.DesignContest =
            AmendFEResult.Tender.DesignContest(
                serviceContractAward = entity.serviceContractAward
            )

        private fun convert(entity: FEEntity.Tender.ElectronicWorkflows): AmendFEResult.Tender.ElectronicWorkflows =
            AmendFEResult.Tender.ElectronicWorkflows(
                useOrdering = entity.useOrdering,
                usePayment = entity.usePayment,
                acceptInvoicing = entity.acceptInvoicing
            )

        private fun convert(entity: FEEntity.Tender.JointProcurement): AmendFEResult.Tender.JointProcurement =
            AmendFEResult.Tender.JointProcurement(
                isJointProcurement = entity.isJointProcurement
            )

        private fun convert(entity: FEEntity.Tender.ProcedureOutsourcing): AmendFEResult.Tender.ProcedureOutsourcing =
            AmendFEResult.Tender.ProcedureOutsourcing(
                procedureOutsourced = entity.procedureOutsourced
            )

        private fun convert(entity: FEEntity.Tender.Framework): AmendFEResult.Tender.Framework =
            AmendFEResult.Tender.Framework(
                isAFramework = entity.isAFramework
            )

        private fun convert(entity: FEEntity.Tender.DynamicPurchasingSystem): AmendFEResult.Tender.DynamicPurchasingSystem =
            AmendFEResult.Tender.DynamicPurchasingSystem(
                hasDynamicPurchasingSystem = entity.hasDynamicPurchasingSystem
            )

        private fun convert(entity: FEEntity.Tender.Document): AmendFEResult.Tender.Document =
            AmendFEResult.Tender.Document(
                documentType = entity.documentType,
                id = entity.id,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.SecondStage): AmendFEResult.Tender.SecondStage =
            AmendFEResult.Tender.SecondStage(
                minimumCandidates = entity.minimumCandidates,
                maximumCandidates = entity.maximumCandidates
            )

        private fun convert(entity: FEEntity.Tender.OtherCriteria): AmendFEResult.Tender.OtherCriteria =
            AmendFEResult.Tender.OtherCriteria(
                reductionCriteria = entity.reductionCriteria,
                qualificationSystemMethods = entity.qualificationSystemMethods
            )

        private fun convert(entity: FEEntity.Tender.ContractPeriod): AmendFEResult.Tender.ContractPeriod =
            AmendFEResult.Tender.ContractPeriod(
                startDate = entity.startDate,
                endDate = entity.endDate
            )

        private fun convert(entity: FEEntity.Tender.Criteria): AmendFEResult.Tender.Criteria =
            AmendFEResult.Tender.Criteria(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                relatesTo = entity.relatesTo,
                source = entity.source,
                classification = entity.classification?.let { convert(it) },
                requirementGroups = entity.requirementGroups.map { convert(it) }
            )

        private fun convert(entity: FEEntity.Tender.Criteria.Classification): AmendFEResult.Tender.Criteria.Classification =
            AmendFEResult.Tender.Criteria.Classification(
                id = entity.id,
                scheme = entity.scheme
            )

        private fun convert(entity: FEEntity.Tender.Criteria.RequirementGroup): AmendFEResult.Tender.Criteria.RequirementGroup =
            AmendFEResult.Tender.Criteria.RequirementGroup(
                id = entity.id,
                description = entity.description,
                requirements = entity.requirements
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity): AmendFEResult.Tender.ProcuringEntity =
            AmendFEResult.Tender.ProcuringEntity(
                id = entity.id,
                name = entity.name
            )

        private fun convert(entity: FEEntity.Tender.Party): AmendFEResult.Tender.Party =
            AmendFEResult.Tender.Party(
                id = entity.id,
                name = entity.name,
                identifier = entity.identifier
                    .let { identifier ->
                        AmendFEResult.Tender.Party.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                additionalIdentifiers = entity.additionalIdentifiers
                    ?.map { additionalIdentifier ->
                        AmendFEResult.Tender.Party.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                address = entity.address
                    .let { address ->
                        AmendFEResult.Tender.Party.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails
                                .let { addressDetails ->
                                    AmendFEResult.Tender.Party.Address.AddressDetails(
                                        country = addressDetails.country
                                            .let { country ->
                                                AmendFEResult.Tender.Party.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                        region = addressDetails.region
                                            .let { region ->
                                                AmendFEResult.Tender.Party.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                        locality = addressDetails.locality
                                            .let { locality ->
                                                AmendFEResult.Tender.Party.Address.AddressDetails.Locality(
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
                contactPoint = entity.contactPoint
                    .let { contactPoint ->
                        AmendFEResult.Tender.Party.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                roles = entity.roles,
                persones = entity.persones?.map { person ->
                    AmendFEResult.Tender.Party.Person(
                        id = person.id,
                        title = person.title,
                        name = person.name,
                        identifier = person.identifier
                            .let { identifier ->
                                AmendFEResult.Tender.Party.Person.Identifier(
                                    id = identifier.id,
                                    scheme = identifier.scheme,
                                    uri = identifier.uri
                                )
                            },
                        businessFunctions = person.businessFunctions
                            .map { businessFunctions ->
                                AmendFEResult.Tender.Party.Person.BusinessFunction(
                                    id = businessFunctions.id,
                                    jobTitle = businessFunctions.jobTitle,
                                    type = businessFunctions.type,
                                    period = businessFunctions.period
                                        .let { period ->
                                            AmendFEResult.Tender.Party.Person.BusinessFunction.Period(
                                                startDate = period.startDate
                                            )
                                        },
                                    documents = businessFunctions.documents
                                        ?.map { document ->
                                            AmendFEResult.Tender.Party.Person.BusinessFunction.Document(
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
    }
}