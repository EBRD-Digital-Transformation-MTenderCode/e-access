package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.infrastructure.entity.FEEntity

class CreateFeEntityConverter {
    companion object {

        fun fromEntity(entity: FEEntity): CreateFEResult =
            CreateFEResult(
                ocid = entity.ocid,
                token = entity.token,
                tender = fromEntity(entity.tender)
            )

        private fun fromEntity(entity: FEEntity.Tender): CreateFEResult.Tender =
            CreateFEResult.Tender(
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

        private fun convert(entity: FEEntity.Tender.Classification): CreateFEResult.Tender.Classification =
            CreateFEResult.Tender.Classification(
                scheme = entity.scheme,
                id = entity.id,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.AcceleratedProcedure): CreateFEResult.Tender.AcceleratedProcedure =
            CreateFEResult.Tender.AcceleratedProcedure(
                isAcceleratedProcedure = entity.isAcceleratedProcedure
            )

        private fun convert(entity: FEEntity.Tender.DesignContest): CreateFEResult.Tender.DesignContest =
            CreateFEResult.Tender.DesignContest(
                serviceContractAward = entity.serviceContractAward
            )

        private fun convert(entity: FEEntity.Tender.ElectronicWorkflows): CreateFEResult.Tender.ElectronicWorkflows =
            CreateFEResult.Tender.ElectronicWorkflows(
                useOrdering = entity.useOrdering,
                usePayment = entity.usePayment,
                acceptInvoicing = entity.acceptInvoicing
            )

        private fun convert(entity: FEEntity.Tender.JointProcurement): CreateFEResult.Tender.JointProcurement =
            CreateFEResult.Tender.JointProcurement(
                isJointProcurement = entity.isJointProcurement
            )

        private fun convert(entity: FEEntity.Tender.ProcedureOutsourcing): CreateFEResult.Tender.ProcedureOutsourcing =
            CreateFEResult.Tender.ProcedureOutsourcing(
                procedureOutsourced = entity.procedureOutsourced
            )

        private fun convert(entity: FEEntity.Tender.Framework): CreateFEResult.Tender.Framework =
            CreateFEResult.Tender.Framework(
                isAFramework = entity.isAFramework
            )

        private fun convert(entity: FEEntity.Tender.DynamicPurchasingSystem): CreateFEResult.Tender.DynamicPurchasingSystem =
            CreateFEResult.Tender.DynamicPurchasingSystem(
                hasDynamicPurchasingSystem = entity.hasDynamicPurchasingSystem
            )

        private fun convert(entity: FEEntity.Tender.Document): CreateFEResult.Tender.Document =
            CreateFEResult.Tender.Document(
                documentType = entity.documentType,
                id = entity.id,
                title = entity.title,
                description = entity.description
            )

        private fun convert(entity: FEEntity.Tender.SecondStage): CreateFEResult.Tender.SecondStage =
            CreateFEResult.Tender.SecondStage(
                minimumCandidates = entity.minimumCandidates,
                maximumCandidates = entity.maximumCandidates
            )

        private fun convert(entity: FEEntity.Tender.OtherCriteria): CreateFEResult.Tender.OtherCriteria =
            CreateFEResult.Tender.OtherCriteria(
                reductionCriteria = entity.reductionCriteria,
                qualificationSystemMethods = entity.qualificationSystemMethods
            )

        private fun convert(entity: FEEntity.Tender.ContractPeriod): CreateFEResult.Tender.ContractPeriod =
            CreateFEResult.Tender.ContractPeriod(
                startDate = entity.startDate,
                endDate = entity.endDate
            )

        private fun convert(entity: FEEntity.Tender.Criteria): CreateFEResult.Tender.Criteria =
            CreateFEResult.Tender.Criteria(
                id = entity.id,
                title = entity.title,
                description = entity.description,
                relatesTo = entity.relatesTo,
                source = entity.source,
                requirementGroups = entity.requirementGroups.map { convert(it) },
                classification = entity.classification
                    ?.let { classification ->
                        CreateFEResult.Tender.Criteria.Classification(
                            id = classification.id,
                            scheme = classification.scheme
                        )
                    }
            )

        private fun convert(entity: FEEntity.Tender.Criteria.RequirementGroup): CreateFEResult.Tender.Criteria.RequirementGroup =
            CreateFEResult.Tender.Criteria.RequirementGroup(
                id = entity.id,
                description = entity.description,
                requirements = entity.requirements
            )

        private fun convert(entity: FEEntity.Tender.ProcuringEntity): CreateFEResult.Tender.ProcuringEntity =
            CreateFEResult.Tender.ProcuringEntity(
                id = entity.id,
                name = entity.name
            )

        private fun convert(entity: FEEntity.Tender.Party): CreateFEResult.Tender.Party =
            CreateFEResult.Tender.Party(
                id = entity.id,
                name = entity.name,
                identifier = entity.identifier
                    .let { identifier ->
                        CreateFEResult.Tender.Party.Identifier(
                            scheme = identifier.scheme,
                            id = identifier.id,
                            legalName = identifier.legalName,
                            uri = identifier.uri
                        )
                    },
                additionalIdentifiers = entity.additionalIdentifiers
                    ?.map { additionalIdentifier ->
                        CreateFEResult.Tender.Party.AdditionalIdentifier(
                            scheme = additionalIdentifier.scheme,
                            id = additionalIdentifier.id,
                            legalName = additionalIdentifier.legalName,
                            uri = additionalIdentifier.uri
                        )
                    },
                address = entity.address
                    .let { address ->
                        CreateFEResult.Tender.Party.Address(
                            streetAddress = address.streetAddress,
                            postalCode = address.postalCode,
                            addressDetails = address.addressDetails
                                .let { addressDetails ->
                                    CreateFEResult.Tender.Party.Address.AddressDetails(
                                        country = addressDetails.country
                                            .let { country ->
                                                CreateFEResult.Tender.Party.Address.AddressDetails.Country(
                                                    scheme = country.scheme,
                                                    id = country.id,
                                                    description = country.description,
                                                    uri = country.uri
                                                )
                                            },
                                        region = addressDetails.region
                                            .let { region ->
                                                CreateFEResult.Tender.Party.Address.AddressDetails.Region(
                                                    scheme = region.scheme,
                                                    id = region.id,
                                                    description = region.description,
                                                    uri = region.uri
                                                )
                                            },
                                        locality = addressDetails.locality
                                            .let { locality ->
                                                CreateFEResult.Tender.Party.Address.AddressDetails.Locality(
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
                        CreateFEResult.Tender.Party.ContactPoint(
                            name = contactPoint.name,
                            email = contactPoint.email,
                            telephone = contactPoint.telephone,
                            faxNumber = contactPoint.faxNumber,
                            url = contactPoint.url
                        )
                    },
                roles = entity.roles,
                persones = entity.persones?.map { person ->
                    CreateFEResult.Tender.Party.Person(
                        id = person.id,
                        title = person.title,
                        name = person.name,
                        identifier = person.identifier
                            .let { identifier ->
                                CreateFEResult.Tender.Party.Person.Identifier(
                                    id = identifier.id,
                                    scheme = identifier.scheme,
                                    uri = identifier.uri
                                )
                            },
                        businessFunctions = person.businessFunctions
                            .map { businessFunctions ->
                                CreateFEResult.Tender.Party.Person.BusinessFunction(
                                    id = businessFunctions.id,
                                    jobTitle = businessFunctions.jobTitle,
                                    type = businessFunctions.type,
                                    period = businessFunctions.period
                                        .let { period ->
                                            CreateFEResult.Tender.Party.Person.BusinessFunction.Period(
                                                startDate = period.startDate
                                            )
                                        },
                                    documents = businessFunctions.documents
                                        ?.map { document ->
                                            CreateFEResult.Tender.Party.Person.BusinessFunction.Document(
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