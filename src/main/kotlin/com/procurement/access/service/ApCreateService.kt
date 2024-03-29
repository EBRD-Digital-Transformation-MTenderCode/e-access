package com.procurement.access.service

import com.procurement.access.application.service.ap.create.ApCreateData
import com.procurement.access.application.service.ap.create.ApCreateResult
import com.procurement.access.application.service.ap.create.CreateApContext
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.SubmissionMethod
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.repository.CassandraTenderProcessRepositoryV1
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration
import java.util.*

@Service
class ApCreateService(
    private val generationService: GenerationService,
    private val rulesService: RulesService,
    private val tenderRepository: CassandraTenderProcessRepositoryV1
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(ApCreateService::class.java)
    }

    fun createAp(contextRequest: CreateApContext, request: ApCreateData): ApCreateResult {
        checkValidationRules(request, contextRequest)
        val cpid = generationService.generateCpid(contextRequest.mode.prefix, contextRequest.country, nowDefaultUTC())
        val ocid = generationService.generateOcid(cpid, Stage.AP.key)

        val apEntity: APEntity = applyBusinessRules(contextRequest, request, ocid)
        val token = generationService.generateToken()
        tenderRepository.save(
            TenderProcessEntity(
                cpId = cpid,
                token = token,
                ocid = ocid,
                owner = contextRequest.owner,
                createdDate = contextRequest.startDate,
                jsonData = toJson(apEntity)
            )
        )
        return getResponse(apEntity, token, cpid)
    }

    /**
     * Validation rules
     */
    private fun checkValidationRules(request: ApCreateData, contextRequest: CreateApContext) {
        //VR-3.1.16
        if (request.tender.title.isBlank())
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "The attribute 'tender.title' is empty or blank."
            )

        //VR-3.1.17
        if (request.tender.description.isBlank())
            throw ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "The attribute 'tender.description' is empty or blank."
            )

        //VR-3.1.6 Tender Period: Start Date
        checkTenderPeriod(tenderPeriod = request.tender.tenderPeriod)

        checkContractPeriod(request.tender.contractPeriod, contextRequest)

        // VR.COM-1.20.1
        checkDocuments(request.tender.documents)
    }

    private fun checkDocuments(documents: List<ApCreateData.Tender.Document>) {
        val docsIds = documents.map { it.id }

        checkDocumentsIds(docsIds)
    }

    private fun checkDocumentsIds(ids: List<String>) {
        val uniqIds = ids.toSet()
        if (ids.size != uniqIds.size)
            throw ErrorException(
                error = ErrorType.NOT_UNIQUE_IDS,
                message = "Tender documents contains duplicated ids."
            )
    }

    /**
     * VR-3.1.6 Tender Period: Start Date
     *
     * eAccess проверяет что, в поле Tender.tenderPeriod.startDate зафиксировано первое календарное число
     * каждого месяца:
     *
     * IF 1 месяц,  tenderPeriod.startDate == "YYYY-01-01Thh:mm:ssZ"
     * IF 2 месяц,  tenderPeriod.startDate == "YYYY-02-01Thh:mm:ssZ"
     * IF 3 месяц,  tenderPeriod.startDate == "YYYY-03-01Thh:mm:ssZ"
     * IF 4 месяц,  tenderPeriod.startDate == "YYYY-04-01Thh:mm:ssZ"
     * IF 5 месяц,  tenderPeriod.startDate == "YYYY-05-01Thh:mm:ssZ"
     * IF 6 месяц,  tenderPeriod.startDate == "YYYY-06-01Thh:mm:ssZ"
     * IF 7 месяц,  tenderPeriod.startDate == "YYYY-07-01Thh:mm:ssZ"
     * IF 8 месяц,  tenderPeriod.startDate == "YYYY-08-01Thh:mm:ssZ"
     * IF 9 месяц,  tenderPeriod.startDate == "YYYY-09-01Thh:mm:ssZ"
     * IF 10 месяц, tenderPeriod.startDate == "YYYY-10-01Thh:mm:ssZ"
     * IF 11 месяц, tenderPeriod.startDate == "YYYY-11-01Thh:mm:ssZ"
     * IF 12 месяц, tenderPeriod.startDate == "YYYY-12-01Thh:mm:ssZ"
     */
    private fun checkTenderPeriod(tenderPeriod: ApCreateData.Tender.TenderPeriod) {
        if (tenderPeriod.startDate.dayOfMonth != 1)
            throw ErrorException(ErrorType.INVALID_START_DATE)
    }


    private fun checkContractPeriod(contractPeriod: ApCreateData.Tender.ContractPeriod, contextRequest: CreateApContext) {
        if (contractPeriod.startDate <= contextRequest.startDate)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER_CONTRACT_PERIOD,
                message = "Contract period start date must be greater than context start date."
            )

        if (contractPeriod.startDate >= contractPeriod.endDate)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER_CONTRACT_PERIOD,
                message = "Contract period start date must precede contract period end date."
            )

        val maxDuration = rulesService.getMaxDurationOfFA(contextRequest.country, contextRequest.pmd)
        val actualDuration = Duration.between(contractPeriod.startDate, contractPeriod.endDate)

        if (actualDuration > maxDuration)
            throw ErrorException(
                error = ErrorType.INVALID_TENDER_CONTRACT_PERIOD,
                message = "Contract period duration must be less than or equal to maximum allowed duration."
            )
    }

    /**
     * Business rules
     */
    private fun applyBusinessRules(contextRequest: CreateApContext, request: ApCreateData, ocid: Ocid): APEntity {
        val documents: List<APEntity.Tender.Document>? = request.tender.documents
            .map { document -> convertRequestDocument(document) }

        return APEntity(
            ocid = ocid.value,
            tender = tender(
                pmd = contextRequest.pmd,
                documents = documents,
                tenderRequest = request.tender
            ),
            parties = request.tender.procuringEntity
                .let { procuringEntity ->
                    APEntity.Party(
                        id = generationService.generateOrganizationId(
                            identifierScheme = procuringEntity.identifier.scheme,
                            identifierId = procuringEntity.identifier.id
                        ),
                        name = procuringEntity.name,
                        identifier = procuringEntity.identifier.let { identifier ->
                            APEntity.Party.Identifier(
                                scheme = identifier.scheme,
                                id = identifier.id,
                                legalName = identifier.legalName,
                                uri = identifier.uri
                            )
                        },
                        additionalIdentifiers = procuringEntity.additionalIdentifiers.map { additionalIdentifier ->
                            APEntity.Party.AdditionalIdentifier(
                                scheme = additionalIdentifier.scheme,
                                id = additionalIdentifier.id,
                                legalName = additionalIdentifier.legalName,
                                uri = additionalIdentifier.uri
                            )
                        },
                        address = procuringEntity.address.let { address ->
                            APEntity.Party.Address(
                                streetAddress = address.streetAddress,
                                postalCode = address.postalCode,
                                addressDetails = address.addressDetails.let { addressDetails ->
                                    APEntity.Party.Address.AddressDetails(
                                        country = addressDetails.country.let { country ->
                                            APEntity.Party.Address.AddressDetails.Country(
                                                scheme = country.scheme,
                                                id = country.id,
                                                description = country.description,
                                                uri = country.uri
                                            )
                                        },
                                        region = addressDetails.region.let { region ->
                                            APEntity.Party.Address.AddressDetails.Region(
                                                scheme = region.scheme,
                                                id = region.id,
                                                description = region.description,
                                                uri = region.uri
                                            )
                                        },
                                        locality = addressDetails.locality.let { locality ->
                                            APEntity.Party.Address.AddressDetails.Locality(
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
                        contactPoint = procuringEntity.contactPoint.let { contactPoint ->
                            APEntity.Party.ContactPoint(
                                name = contactPoint.name,
                                email = contactPoint.email,
                                telephone = contactPoint.telephone,
                                faxNumber = contactPoint.faxNumber,
                                url = contactPoint.url
                            )
                        },
                        roles = listOf(PartyRole.CENTRAL_PURCHASING_BODY)
                    )
                }
                .let { listOf(it) },
            relatedProcesses = null
        )
    }

    private fun tender(
        pmd: ProcurementMethod,
        documents: List<APEntity.Tender.Document>?,
        tenderRequest: ApCreateData.Tender
    ): APEntity.Tender {
        return APEntity.Tender(
            //BR-3.1.4
            id = generationService.generatePermanentTenderId(),
            /** Begin BR-3.1.2*/
            status = TenderStatus.PLANNING,
            statusDetails = TenderStatusDetails.AGGREGATION,
            /** End BR-3.1.2*/

            classification = tenderRequest.classification.let { classification ->
                APEntity.Tender.Classification(
                    scheme = classification.scheme,
                    id = classification.id,
                    description = classification.description
                )
            },
            value = APEntity.Tender.Value(amount = null, currency = tenderRequest.value.currency),
            title = tenderRequest.title,
            description = tenderRequest.description,
            //BR-3.1.17
            acceleratedProcedure = APEntity.Tender.AcceleratedProcedure(isAcceleratedProcedure = false),
            //BR-3.1.7
            designContest = APEntity.Tender.DesignContest(serviceContractAward = false),
            //BR-3.1.8, BR-3.1.9, BR-3.1.10
            electronicWorkflows = APEntity.Tender.ElectronicWorkflows(
                useOrdering = false,
                usePayment = false,
                acceptInvoicing = false
            ),
            //BR-3.1.11
            jointProcurement = APEntity.Tender.JointProcurement(isJointProcurement = false),
            //BR-3.1.12
            procedureOutsourcing = APEntity.Tender.ProcedureOutsourcing(procedureOutsourced = false),
            //BR-3.1.13
            framework = APEntity.Tender.Framework(isAFramework = true),
            //BR-3.1.14
            dynamicPurchasingSystem = APEntity.Tender.DynamicPurchasingSystem(
                hasDynamicPurchasingSystem = hasDynamicPurchasingSystem(pmd = pmd)
            ),
            legalBasis = tenderRequest.legalBasis,
            procurementMethod = pmd,
            procurementMethodDetails = tenderRequest.procurementMethodDetails,
            procurementMethodRationale = tenderRequest.procurementMethodRationale,
            eligibilityCriteria = tenderRequest.eligibilityCriteria,
            tenderPeriod = tenderRequest.tenderPeriod.let { period ->
                APEntity.Tender.TenderPeriod(
                    startDate = period.startDate
                )
            },
            contractPeriod = tenderRequest.contractPeriod.let { period ->
                APEntity.Tender.ContractPeriod(
                    startDate = period.startDate,
                    endDate = period.endDate
                )
            },
            //BR-3.1.16
            requiresElectronicCatalogue = false,
            //BR-3.1.18
            submissionMethod = listOf(SubmissionMethod.ELECTRONIC_SUBMISSION),
            submissionMethodRationale = tenderRequest.submissionMethodRationale,
            submissionMethodDetails = tenderRequest.submissionMethodDetails,
            documents = documents,

            items = emptyList(),
            lots = emptyList(),
            mainProcurementCategory = null
        )
    }

    fun hasDynamicPurchasingSystem(pmd: ProcurementMethod): Boolean = when (pmd) {
        ProcurementMethod.CD, ProcurementMethod.TEST_CD,
        ProcurementMethod.CF, ProcurementMethod.TEST_CF,
        ProcurementMethod.DA, ProcurementMethod.TEST_DA,
        ProcurementMethod.DC, ProcurementMethod.TEST_DC,
        ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
        ProcurementMethod.FA, ProcurementMethod.TEST_FA,
        ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
        ProcurementMethod.IP, ProcurementMethod.TEST_IP,
        ProcurementMethod.MC, ProcurementMethod.TEST_MC,
        ProcurementMethod.MV, ProcurementMethod.TEST_MV,
        ProcurementMethod.NP, ProcurementMethod.TEST_NP,
        ProcurementMethod.OP, ProcurementMethod.TEST_OP,
        ProcurementMethod.OT, ProcurementMethod.TEST_OT,
        ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
        ProcurementMethod.RT, ProcurementMethod.TEST_RT,
        ProcurementMethod.SV, ProcurementMethod.TEST_SV -> false

        ProcurementMethod.OF, ProcurementMethod.TEST_OF -> true
    }

    private fun convertRequestDocument(documentFromRequest: ApCreateData.Tender.Document): APEntity.Tender.Document {
        return APEntity.Tender.Document(
            id = documentFromRequest.id,
            documentType = DocumentType.creator(documentFromRequest.documentType.key),
            title = documentFromRequest.title,
            description = documentFromRequest.description,
            relatedLots = emptyList()
        )
    }

    private fun getResponse(cn: APEntity, token: UUID, cpid: Cpid): ApCreateResult {
        return ApCreateResult(
            cpid = cpid,
            ocid = cn.ocid,
            token = token.toString(),
            tender = cn.tender
                .let { tender ->
                    ApCreateResult.Tender(
                        id = tender.id,
                        status = tender.status,
                        statusDetails = tender.statusDetails,
                        title = tender.title,
                        description = tender.description,
                        classification = tender.classification
                            .let { classification ->
                                ApCreateResult.Tender.Classification(
                                    scheme = classification.scheme,
                                    id = classification.id,
                                    description = classification.description
                                )
                            },
                        value = ApCreateResult.Tender.Value(currency = tender.value.currency),
                        tenderPeriod = tender.tenderPeriod
                            .let { tenderPeriod ->
                                ApCreateResult.Tender.TenderPeriod(
                                    startDate = tenderPeriod.startDate
                                )
                            },
                        contractPeriod = tender.contractPeriod
                            !!.let { contractPeriod ->
                                ApCreateResult.Tender.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                        acceleratedProcedure = tender.acceleratedProcedure
                            .let { acceleratedProcedure ->
                                ApCreateResult.Tender.AcceleratedProcedure(
                                    isAcceleratedProcedure = acceleratedProcedure.isAcceleratedProcedure
                                )
                            },
                        designContest = tender.designContest
                            .let { designContest ->
                                ApCreateResult.Tender.DesignContest(
                                    serviceContractAward = designContest.serviceContractAward
                                )
                            },
                        electronicWorkflows = tender.electronicWorkflows
                            .let { electronicWorkflows ->
                                ApCreateResult.Tender.ElectronicWorkflows(
                                    useOrdering = electronicWorkflows.useOrdering,
                                    usePayment = electronicWorkflows.usePayment,
                                    acceptInvoicing = electronicWorkflows.acceptInvoicing
                                )
                            },
                        jointProcurement = tender.jointProcurement
                            .let { jointProcurement ->
                                ApCreateResult.Tender.JointProcurement(
                                    isJointProcurement = jointProcurement.isJointProcurement
                                )
                            },
                        procedureOutsourcing = tender.procedureOutsourcing
                            .let { procedureOutsourcing ->
                                ApCreateResult.Tender.ProcedureOutsourcing(
                                    procedureOutsourced = procedureOutsourcing.procedureOutsourced
                                )
                            },
                        framework = tender.framework
                            .let { framework ->
                                ApCreateResult.Tender.Framework(
                                    isAFramework = framework.isAFramework
                                )
                            },
                        dynamicPurchasingSystem = tender.dynamicPurchasingSystem
                            .let { dynamicPurchasingSystem ->
                                ApCreateResult.Tender.DynamicPurchasingSystem(
                                    hasDynamicPurchasingSystem = dynamicPurchasingSystem.hasDynamicPurchasingSystem
                                )
                            },
                        legalBasis = tender.legalBasis,
                        procurementMethod = tender.procurementMethod,
                        procurementMethodDetails = tender.procurementMethodDetails,
                        procurementMethodRationale = tender.procurementMethodRationale,
                        eligibilityCriteria = tender.eligibilityCriteria,
                        requiresElectronicCatalogue = tender.requiresElectronicCatalogue,
                        submissionMethod = tender.submissionMethod,
                        submissionMethodRationale = tender.submissionMethodRationale,
                        submissionMethodDetails = tender.submissionMethodDetails,
                        documents = tender.documents
                            ?.map { document ->
                                ApCreateResult.Tender.Document(
                                    documentType = document.documentType,
                                    id = document.id,
                                    title = document.title!!,
                                    description = document.description
                                )
                            }
                            .orEmpty()
                    )
                },
            parties = cn.parties
                .map { party ->
                    ApCreateResult.Party(
                        id = party.id,
                        name = party.name,
                        identifier = party.identifier
                            .let { identifier ->
                                ApCreateResult.Party.Identifier(
                                    scheme = identifier.scheme,
                                    id = identifier.id,
                                    legalName = identifier.legalName,
                                    uri = identifier.uri
                                )
                            },
                        additionalIdentifiers = party.additionalIdentifiers
                            ?.map { additionalIdentifier ->
                                ApCreateResult.Party.AdditionalIdentifier(
                                    scheme = additionalIdentifier.scheme,
                                    id = additionalIdentifier.id,
                                    legalName = additionalIdentifier.legalName,
                                    uri = additionalIdentifier.uri
                                )
                            }
                            .orEmpty(),
                        address = party.address
                            .let { address ->
                                ApCreateResult.Party.Address(
                                    streetAddress = address.streetAddress,
                                    postalCode = address.postalCode,
                                    addressDetails = address.addressDetails
                                        .let { addressDetails ->
                                            ApCreateResult.Party.Address.AddressDetails(
                                                country = addressDetails.country
                                                    .let { country ->
                                                        ApCreateResult.Party.Address.AddressDetails.Country(
                                                            scheme = country.scheme,
                                                            id = country.id,
                                                            description = country.description,
                                                            uri = country.uri
                                                        )
                                                    },
                                                region = addressDetails.region
                                                    .let { region ->
                                                        ApCreateResult.Party.Address.AddressDetails.Region(
                                                            scheme = region.scheme,
                                                            id = region.id,
                                                            description = region.description,
                                                            uri = region.uri
                                                        )
                                                    },
                                                locality = addressDetails.locality
                                                    .let { locality ->
                                                        ApCreateResult.Party.Address.AddressDetails.Locality(
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
                        contactPoint = party.contactPoint
                            .let { contactPoint ->
                                ApCreateResult.Party.ContactPoint(
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
}
