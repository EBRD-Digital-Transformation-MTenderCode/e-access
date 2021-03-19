package com.procurement.access.service

import com.procurement.access.application.service.fe.create.CreateFEContext
import com.procurement.access.application.service.fe.create.CreateFEData
import com.procurement.access.application.service.fe.create.CreateFEResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.RequirementStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.money.Money
import com.procurement.access.domain.model.persone.PersonId
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.handler.v1.converter.CreateFeEntityConverter
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

interface FeCreateService {
    fun createFe(context: CreateFEContext, request: CreateFEData): CreateFEResult
}

@Service
class FeCreateServiceImpl(
    private val generationService: GenerationService,
    private val tenderProcessDao: TenderProcessDao
) : FeCreateService {
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
        val ocid = generationService.generateOcid(cpid = cpid, stage = context.stage)

        val fe = createEntity(ocid = ocid, token = entity.token, datePublished = context.startDate, data = request, ap = ap)

        val result = CreateFeEntityConverter.fromEntity(fe);

        tenderProcessDao.save(
            TenderProcessEntity(
                cpId = cpid,
                token = entity.token,
                stage = context.stage,
                owner = context.owner,
                createdDate = context.startDate,
                jsonData = toJson(fe)
            )
        )

        return result
    }

    private fun createEntity(ocid: Ocid, token: UUID, data: CreateFEData, ap: APEntity, datePublished: LocalDateTime): FEEntity {
        val parties = createParties(data, ap)
        return FEEntity(
            ocid = ocid.toString(),
            token = token.toString(),
            tender = FEEntity.Tender(
                id = generationService.generatePermanentTenderId(),
                status = TenderStatus.ACTIVE, // BR-1.0.1.4.2
                statusDetails = TenderStatusDetails.SUBMISSION,  // BR-1.0.1.4.2
                title = data.tender.title,
                description = data.tender.description,
                secondStage = data.tender.secondStage?.convert(),
                procurementMethodRationale = data.tender.procurementMethodRationale ?: ap.tender.procurementMethodRationale,
                parties = parties,
                procuringEntity = createProcuringEntity(parties),
                criteria = data.tender.criteria
                    .map { criterion ->
                        FEEntity.Tender.Criteria(
                            id = generationService.criterionId(), // BR-1.0.1.16.1
                            description = criterion.description,
                            title = criterion.title,
                            source = CriteriaSource.TENDERER, // BR-1.0.1.16.2
                            relatesTo = criterion.relatesTo,
                            classification = criterion.classification.let { classification ->   //BR-1.0.1.16.3
                                FEEntity.Tender.Criteria.Classification(
                                    id = classification.id,
                                    scheme = classification.scheme
                                )
                            },
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
                                                    value = requirement.value,
                                                    eligibleEvidences = requirement.eligibleEvidences?.toList(),
                                                    status = RequirementStatus.ACTIVE,
                                                    datePublished = datePublished
                                                )
                                            }
                                    )
                                }
                        )
                    },
                otherCriteria = data.tender.otherCriteria?.convert(),
                procurementMethodModalities = data.tender.procurementMethodModalities,
                documents = data.tender.documents.map { it.convert() },
                classification = ap.tender.classification.convert(),
                value = ap.tender.value.let { value ->
                    Money(amount = value.amount!!, currency = value.currency)
                },
                contractPeriod = ap.tender.contractPeriod!!.convert(),
                acceleratedProcedure = ap.tender.acceleratedProcedure.convert(),
                designContest = ap.tender.designContest.convert(),
                electronicWorkflows = ap.tender.electronicWorkflows.convert(),
                jointProcurement = ap.tender.jointProcurement.convert(),
                dynamicPurchasingSystem = ap.tender.dynamicPurchasingSystem.convert(),
                legalBasis = ap.tender.legalBasis,
                procurementMethod = ap.tender.procurementMethod,
                procurementMethodDetails = ap.tender.procurementMethodDetails,
                eligibilityCriteria = ap.tender.eligibilityCriteria,
                requiresElectronicCatalogue = ap.tender.requiresElectronicCatalogue,
                submissionMethod = ap.tender.submissionMethod,
                submissionMethodDetails = ap.tender.submissionMethodDetails,
                submissionMethodRationale = ap.tender.submissionMethodRationale,
                procedureOutsourcing = ap.tender.procedureOutsourcing.convert(),
                mainProcurementCategory = ap.tender.mainProcurementCategory,
                framework = FEEntity.Tender.Framework(isAFramework = true)
            ),
            relatedProcesses = emptyList()
        )
    }

    private fun createProcuringEntity(parties: List<FEEntity.Tender.Party>): FEEntity.Tender.ProcuringEntity {
        val party = parties.first { it.roles.contains(PartyRole.PROCURING_ENTITY) }
        return FEEntity.Tender.ProcuringEntity(id = party.id, name = party.name)
    }

    private fun createParties(data: CreateFEData, ap: APEntity): List<FEEntity.Tender.Party> {
        val cplRole = PartyRole.CENTRAL_PURCHASING_BODY
        val cpbPersones = data.tender.procuringEntity?.persons?.map { it.convert() }

        val cpbParty = ap.tender.parties
            .firstOrNull { it.roles.contains(cplRole) }
            ?.convert()
            ?.copy(roles = listOf(PartyRole.PROCURING_ENTITY), persones = cpbPersones)
            ?: throw ErrorException(ErrorType.MISSING_PARTIES, "Party with role '$cplRole' not found.")

        val clientParties = ap.tender.parties
            .filter { it.roles.contains(PartyRole.CLIENT) }
            .map { it.convert() }
            .map { it.copy(roles = listOf(PartyRole.BUYER)) }

        return clientParties + cpbParty
    }

    private fun APEntity.Tender.Party.convert(): FEEntity.Tender.Party =
        FEEntity.Tender.Party(
            id = id,
            name = name,
            identifier = identifier
                .let { identifier ->
                    FEEntity.Tender.Party.Identifier(
                        scheme = identifier.scheme,
                        id = identifier.id,
                        legalName = identifier.legalName,
                        uri = identifier.uri
                    )
                },
            additionalIdentifiers = additionalIdentifiers
                ?.map { additionalIdentifier ->
                    FEEntity.Tender.Party.AdditionalIdentifier(
                        scheme = additionalIdentifier.scheme,
                        id = additionalIdentifier.id,
                        legalName = additionalIdentifier.legalName,
                        uri = additionalIdentifier.uri
                    )
                },
            address = address
                .let { address ->
                    FEEntity.Tender.Party.Address(
                        streetAddress = address.streetAddress,
                        postalCode = address.postalCode,
                        addressDetails = address.addressDetails
                            .let { addressDetails ->
                                FEEntity.Tender.Party.Address.AddressDetails(
                                    country = addressDetails.country
                                        .let { country ->
                                            FEEntity.Tender.Party.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                    region = addressDetails.region
                                        .let { region ->
                                            FEEntity.Tender.Party.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                    locality = addressDetails.locality
                                        .let { locality ->
                                            FEEntity.Tender.Party.Address.AddressDetails.Locality(
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
                    FEEntity.Tender.Party.ContactPoint(
                        name = contactPoint.name,
                        email = contactPoint.email,
                        telephone = contactPoint.telephone,
                        faxNumber = contactPoint.faxNumber,
                        url = contactPoint.url
                    )
                },
            roles = roles,
            persones = null
        )

    private fun CreateFEData.Tender.ProcuringEntity.Person.convert(): FEEntity.Tender.Party.Person =
        FEEntity.Tender.Party.Person(
            id = PersonId.parse(id)!!,
            title = title,
            name = name,
            identifier = identifier
                .let { identifier ->
                    FEEntity.Tender.Party.Person.Identifier(
                        id = identifier.id,
                        scheme = identifier.scheme,
                        uri = identifier.uri
                    )
                },
            businessFunctions = businessFunctions
                .map { businessFunctions ->
                    FEEntity.Tender.Party.Person.BusinessFunction(
                        id = businessFunctions.id,
                        jobTitle = businessFunctions.jobTitle,
                        type = businessFunctions.type,
                        period = businessFunctions.period
                            .let { period ->
                                FEEntity.Tender.Party.Person.BusinessFunction.Period(
                                    startDate = period.startDate
                                )
                            },
                        documents = businessFunctions.documents
                            .map { document ->
                                FEEntity.Tender.Party.Person.BusinessFunction.Document(
                                    id = document.id,
                                    title = document.title,
                                    description = document.description,
                                    documentType = document.documentType
                                )
                            }
                    )
                }
        )

    private fun CreateFEData.Tender.SecondStage.convert(): FEEntity.Tender.SecondStage =
        FEEntity.Tender.SecondStage(
            minimumCandidates = this.minimumCandidates,
            maximumCandidates = this.maximumCandidates
        )

    private fun CreateFEData.Tender.OtherCriteria.convert(): FEEntity.Tender.OtherCriteria =
        FEEntity.Tender.OtherCriteria(
            reductionCriteria = this.reductionCriteria,
            qualificationSystemMethods = this.qualificationSystemMethods
        )

    private fun CreateFEData.Tender.Document.convert(): FEEntity.Tender.Document =
        FEEntity.Tender.Document(
            id = this.id,
            documentType = this.documentType,
            title = this.title,
            description = this.description
        )

    private fun APEntity.Tender.Classification.convert(): FEEntity.Tender.Classification =
        FEEntity.Tender.Classification(
            scheme = this.scheme,
            id = this.id,
            description = this.description
        )

    private fun APEntity.Tender.ContractPeriod.convert(): FEEntity.Tender.ContractPeriod =
        FEEntity.Tender.ContractPeriod(
            startDate = this.startDate,
            endDate = this.endDate
        )

    private fun APEntity.Tender.AcceleratedProcedure.convert(): FEEntity.Tender.AcceleratedProcedure =
        FEEntity.Tender.AcceleratedProcedure(
            isAcceleratedProcedure = this.isAcceleratedProcedure
        )

    private fun APEntity.Tender.DesignContest.convert(): FEEntity.Tender.DesignContest =
        FEEntity.Tender.DesignContest(
            serviceContractAward = this.serviceContractAward
        )

    private fun APEntity.Tender.ElectronicWorkflows.convert(): FEEntity.Tender.ElectronicWorkflows =
        FEEntity.Tender.ElectronicWorkflows(
            useOrdering = this.useOrdering,
            acceptInvoicing = this.acceptInvoicing,
            usePayment = this.usePayment
        )

    private fun APEntity.Tender.JointProcurement.convert(): FEEntity.Tender.JointProcurement =
        FEEntity.Tender.JointProcurement(
            isJointProcurement = this.isJointProcurement
        )

    private fun APEntity.Tender.DynamicPurchasingSystem.convert(): FEEntity.Tender.DynamicPurchasingSystem =
        FEEntity.Tender.DynamicPurchasingSystem(
            hasDynamicPurchasingSystem = this.hasDynamicPurchasingSystem
        )

    private fun APEntity.Tender.ProcedureOutsourcing.convert(): FEEntity.Tender.ProcedureOutsourcing =
        FEEntity.Tender.ProcedureOutsourcing(
            procedureOutsourced = this.procedureOutsourced
        )
}
