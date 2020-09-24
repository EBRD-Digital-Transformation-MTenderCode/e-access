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
                secondStage = data.tender.secondStage?.convert(),
                procurementMethodRationale = data.tender.procurementMethodRationale ?: ap.tender.procurementMethodRationale,
                procuringEntity = FEEntity.Tender.ProcuringEntity(
                    id = ap.tender.procuringEntity.id,
                    identifier = ap.tender.procuringEntity.identifier.convert(),
                    name = ap.tender.procuringEntity.name,
                    address = ap.tender.procuringEntity.address.convert(),
                    additionalIdentifiers = ap.tender.procuringEntity.additionalIdentifiers
                        ?.map { it.convert() }
                        .orEmpty(),
                    contactPoint = ap.tender.procuringEntity.contactPoint.convert(),
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
                otherCriteria = data.tender.otherCriteria?.convert(),
                procurementMethodModalities = data.tender.procurementMethodModalities,
                documents = data.tender.documents.map { it.convert() },
                classification = ap.tender.classification.convert(),
                value = ap.tender.value!!,
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
            )
        )

    private fun CreateFEData.Tender.SecondStage.convert(): FEEntity.Tender.SecondStage =
        FEEntity.Tender.SecondStage(
            minimumCandidates = this.minimumCandidates,
            maximumCandidates = this.maximumCandidates
        )

    private fun APEntity.Tender.ProcuringEntity.Identifier.convert(): FEEntity.Tender.ProcuringEntity.Identifier =
        FEEntity.Tender.ProcuringEntity.Identifier(
            id = this.id,
            scheme = this.scheme,
            legalName = this.legalName,
            uri = this.uri
        )

    private fun APEntity.Tender.ProcuringEntity.AdditionalIdentifier.convert(): FEEntity.Tender.ProcuringEntity.Identifier =
        FEEntity.Tender.ProcuringEntity.Identifier(
            id = this.id,
            scheme = this.scheme,
            legalName = this.legalName,
            uri = this.uri
        )

    private fun APEntity.Tender.ProcuringEntity.Address.convert(): FEEntity.Tender.ProcuringEntity.Address =
        FEEntity.Tender.ProcuringEntity.Address(
            streetAddress = this.streetAddress,
            postalCode = this.postalCode,
            addressDetails = this.addressDetails.convert()
        )

    private fun APEntity.Tender.ProcuringEntity.Address.AddressDetails.convert(): FEEntity.Tender.ProcuringEntity.Address.AddressDetails =
        FEEntity.Tender.ProcuringEntity.Address.AddressDetails(
            country = this.country.convert(),
            region = this.region.convert(),
            locality = this.locality.convert()
        )

    private fun APEntity.Tender.ProcuringEntity.Address.AddressDetails.Country.convert(): FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Country =
        FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Country(
            scheme = this.scheme,
            id = this.id,
            description = this.description,
            uri = this.uri
        )

    private fun APEntity.Tender.ProcuringEntity.Address.AddressDetails.Region.convert(): FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Region =
        FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Region(
            scheme = this.scheme,
            id = this.id,
            description = this.description,
            uri = this.uri
        )

    private fun APEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality.convert(): FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality =
        FEEntity.Tender.ProcuringEntity.Address.AddressDetails.Locality(
            scheme = this.scheme,
            id = this.id,
            description = this.description,
            uri = this.uri
        )

    private fun APEntity.Tender.ProcuringEntity.ContactPoint.convert(): FEEntity.Tender.ProcuringEntity.ContactPoint =
        FEEntity.Tender.ProcuringEntity.ContactPoint(
            name = this.name,
            email = this.email,
            telephone = this.telephone,
            faxNumber = this.faxNumber,
            url = this.url
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
