package com.procurement.access.service

import com.procurement.access.application.service.fe.create.CreateFEContext
import com.procurement.access.application.service.fe.create.CreateFEData
import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement
import com.procurement.access.infrastructure.dto.fe.check.converter.CreateFeEntityConverter
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

interface FeCreateService {
    fun createFe(context: CreateFEContext, request: CreateFEData): CreateFEResult
}

@Service
class FeCreateServiceImpl(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao
): FeCreateService {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(FeCreateService::class.java)
    }

    override fun createFe(context: CreateFEContext, request: CreateFEData): CreateFEResult {
        val cpid = context.cpid
        val stage = context.prevStage

        val entity = tenderProcessDao.getByCpIdAndStage(cpId = cpid, stage = stage)
            ?: throw ErrorException(
                error = ErrorType.ENTITY_NOT_FOUND,
                message = "Cannot find tender by cpid='$cpid' and stage='$stage'."
            )

        val ap = toObject(APEntity::class.java, entity.jsonData)

        // BR-1.0.1.21.1
        val ocid = generationService.generateOcid(cpid = cpid, stage = stage)

        val fe = createEntity(ocid = ocid, token = entity.token, data = request, ap = ap)

        val result = CreateFeEntityConverter.fromEntity(fe);

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = cpid,
                token = entity.token,
                stage = context.stage,
                owner = context.owner,
                createdDate = context.startDate.toDate(),
                jsonData = toJson(fe)
            )
        )

        return result
    }

    private fun createEntity(ocid: Ocid, token: UUID, data: CreateFEData, ap: APEntity): FEEntity =
        FEEntity(
            ocid = ocid.toString(),
            token = token.toString(),
            tender = FEEntity.Tender(
                id = generationService.generatePermanentTenderId(),
                status = TenderStatus.ACTIVE, // BR-1.0.1.4.2
                statusDetails = TenderStatusDetails.SUBMISSION,  // BR-1.0.1.4.2
                title = data.tender.title,
                description = data.tender.description,
                secondStage = data.tender.secondStage
                    ?.let { secondStage ->
                        FEEntity.Tender.SecondStage(
                            minimumCandidates = secondStage.minimumCandidates,
                            maximumCandidates = secondStage.maximumCandidates
                        )
                    },
                procurementMethodRationale = data.tender.procurementMethodRationale ?: ap.tender.procurementMethodRationale,
                procuringEntity = FEEntity.Tender.ProcuringEntity(
                    id = ap.tender.procuringEntity.id,
                    identifier = ap.tender.procuringEntity.identifier
                        .let { identifier ->
                            FEEntity.Tender.ProcuringEntity.Identifier(
                                id = identifier.id,
                                scheme = identifier.scheme,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                    name = ap.tender.procuringEntity.name,
                    address = ap.tender.procuringEntity.address
                        .let { address ->
                            FEEntity.Tender.ProcuringEntity.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails
                                    .let { addressDetails ->
                                        FEEntity.Tender.ProcuringEntity.Address.AddressDetails(
                                            country = addressDetails.country
                                                .let { country ->
                                                    FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
                                                        id = country.id,
                                                        scheme = country.scheme,
                                                        description = country.description,
                                                        uri = country.uri
                                                    )
                                                },
                                            region = addressDetails.region
                                                .let { region ->
                                                    FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
                                                        id = region.id,
                                                        scheme = region.scheme,
                                                        description = region.description,
                                                        uri = region.uri
                                                    )
                                                },
                                            locality = addressDetails.locality
                                                .let { locality ->
                                                    FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality(
                                                        id = locality.id,
                                                        scheme = locality.scheme,
                                                        description = locality.description,
                                                        uri = locality.uri
                                                    )
                                                }
                                        )
                                    }
                            )
                        },
                    additionalIdentifiers = ap.tender.procuringEntity.additionalIdentifiers
                        ?.map { additionalIdentifier ->
                            FEEntity.Tender.ProcuringEntity.Identifier(
                                id = additionalIdentifier.id,
                                scheme = additionalIdentifier.scheme,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        }
                        .orEmpty(),
                    contactPoint = ap.tender.procuringEntity.contactPoint
                        .let { contactPoint ->
                            FEEntity.Tender.ProcuringEntity.ContactPoint(
                                name = contactPoint.name,
                                email = contactPoint.email,
                                faxNumber = contactPoint.faxNumber,
                                telephone = contactPoint.telephone,
                                url = contactPoint.url
                            )
                        },
                    persons = data.tender.procuringEntity?.persons
                        ?.map { person ->
                            FEEntity.Tender.ProcuringEntity.Person(
                                id = PersonId.generate(
                                    id = person.identifier.id,
                                    scheme = person.identifier.scheme
                                ).toString(),
                                title = person.title,
                                name = person.name,
                                identifier = person.identifier
                                    .let { identifier ->
                                        FEEntity.Tender.ProcuringEntity.Person.Identifier(
                                            id = identifier.id,
                                            scheme = identifier.scheme,
                                            uri = identifier.uri
                                        )
                                    },
                                businessFunctions = person.businessFunctions
                                    .map { businessFunctions ->
                                        FEEntity.Tender.ProcuringEntity.Person.BusinessFunction(
                                            id = businessFunctions.id,
                                            jobTitle = businessFunctions.jobTitle,
                                            type = businessFunctions.type,
                                            period = businessFunctions.period
                                                .let { period ->
                                                    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Period(
                                                        startDate = period.startDate
                                                    )
                                                },
                                            documents = businessFunctions.documents
                                                .map { document ->
                                                    FEEntity.Tender.ProcuringEntity.Person.BusinessFunction.Document(
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
                        .orEmpty()
                ),
                criteria = data.tender.criteria
                    .map { criterion ->
                        FEEntity.Tender.Criteria(
                            id = generationService.criterionId(), // BR-1.0.1.16.1
                            description = criterion.description,
                            title = criterion.title,
                            source = CriteriaSource.TENDERER, // BR-1.0.1.16.2
                            relatesTo = criterion.relatesTo,
                            requirementGroups = criterion.requirementGroups
                                .map { requirementGroups ->
                                    FEEntity.Tender.Criteria.RequirementGroup(
                                        id = generationService.requirementGroupId(), // BR-1.0.1.17.1
                                        description = requirementGroups.description,
                                        requirements = requirementGroups.requirements
                                            .map { requirement ->
                                                Requirement(
                                                    id = generationService.requirementId(), // BR-1.0.1.18.1
                                                    title = requirement.title,
                                                    description = requirement.description,
                                                    period = requirement.period
                                                        ?.let { period ->
                                                            Requirement.Period(
                                                                startDate = period.startDate,
                                                                endDate = period.endDate
                                                            )
                                                        },
                                                    dataType = requirement.dataType,
                                                    value = requirement.value
                                                )
                                            }
                                    )
                                }
                        )
                    },
                otherCriteria = data.tender.otherCriteria
                    ?.let { otherCriteria ->
                        FEEntity.Tender.OtherCriteria(
                            reductionCriteria = otherCriteria.reductionCriteria,
                            qualificationSystemMethods = otherCriteria.qualificationSystemMethods
                        )
                    },
                procurementMethodModalities = data.tender.procurementMethodModalities,
                documents = data.tender.documents
                    .map { document ->
                        FEEntity.Tender.Document(
                            id = document.id,
                            description = document.description,
                            title = document.title,
                            documentType = document.documentType
                        )
                    },
                classification = ap.tender.classification
                    .let { classification ->
                        FEEntity.Tender.Classification(
                            id = classification.id,
                            scheme = classification.scheme,
                            description = classification.description
                        )
                    },
                value = ap.tender.value!!,
                contractPeriod = ap.tender.contractPeriod!!
                    .let { contractPeriod ->
                        FEEntity.Tender.ContractPeriod(
                            startDate = contractPeriod.startDate,
                            endDate = contractPeriod.endDate
                        )
                    },
                acceleratedProcedure = ap.tender.acceleratedProcedure
                    .let { acceleratedProcedure ->
                        FEEntity.Tender.AcceleratedProcedure(
                            isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                        )
                    },
                designContest = ap.tender.designContest
                    .let { designContest ->
                        FEEntity.Tender.DesignContest(
                            serviceContractAward = designContest.serviceContractAward
                        )
                    },
                electronicWorkflows = ap.tender.electronicWorkflows
                    .let { electronicWorkflows ->
                        FEEntity.Tender.ElectronicWorkflows(
                            useOrdering = electronicWorkflows.useOrdering,
                            acceptInvoicing = electronicWorkflows.acceptInvoicing,
                            usePayment = electronicWorkflows.usePayment
                        )
                    },
                jointProcurement = ap.tender.jointProcurement
                    .let { jointProcurement ->
                        FEEntity.Tender.JointProcurement(
                            isJointProcurement = jointProcurement.isJointProcurement
                        )
                    },
                dynamicPurchasingSystem = ap.tender.dynamicPurchasingSystem
                    .let { dynamicPurchasingSystem ->
                        FEEntity.Tender.DynamicPurchasingSystem(
                            hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                        )
                    },
                legalBasis = ap.tender.legalBasis,
                procurementMethod = ap.tender.procurementMethod,
                procurementMethodDetails = ap.tender.procurementMethodDetails,
                eligibilityCriteria = ap.tender.eligibilityCriteria,
                requiresElectronicCatalogue = ap.tender.requiresElectronicCatalogue,
                submissionMethod = ap.tender.submissionMethod,
                submissionMethodDetails = ap.tender.submissionMethodDetails,
                submissionMethodRationale = ap.tender.submissionMethodRationale,
                procedureOutsourcing = ap.tender.procedureOutsourcing
                    .let { procedureOutsourcing ->
                        FEEntity.Tender.ProcedureOutsourcing(
                            procedureOutsourced = procedureOutsourcing.procedureOutsourced
                        )
                    },
                mainProcurementCategory = ap.tender.mainProcurementCategory,
                framework = FEEntity.Tender.Framework(
                    isAFramework = true
                )
            )
        )
}
