package com.procurement.access.service

import com.procurement.access.application.model.params.CreateRelationToContractProcessStageParams
import com.procurement.access.application.model.params.CreateRfqParams
import com.procurement.access.application.model.params.ValidateRfqDataParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.CommandValidationErrors
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.AwardCriteria
import com.procurement.access.domain.model.enums.AwardCriteriaDetails
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.configuration.properties.UriProperties
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRelationToContractProcessStageResult
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRfqResult
import com.procurement.access.lib.extension.getDuplicate
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

interface RfqService {
    fun validateRfqData(params: ValidateRfqDataParams): ValidationResult<Fail>
    fun createRfq(params: CreateRfqParams): Result<CreateRfqResult, Fail>
    fun createRelationToContractProcessStage(params: CreateRelationToContractProcessStageParams): Result<CreateRelationToContractProcessStageResult, Fail>
}

@Service
class RfqServiceImpl(
    private val tenderProcessRepository: TenderProcessRepository,
    private val generationService: GenerationService,
    private val uriProperties: UriProperties,
    private val transform: Transform
) : RfqService {
    override fun validateRfqData(params: ValidateRfqDataParams): ValidationResult<Fail> {
        val pnEntity = tenderProcessRepository.getByCpIdAndOcid(params.relatedCpid, params.relatedOcid)
            .onFailure { return it.reason.asValidationFailure() }
            ?: return CommandValidationErrors.ValidateRfqData.PnNotFound(params.relatedCpid, params.relatedOcid)
                .asValidationFailure()

        val pn = pnEntity.jsonData.tryToObject(PNEntity::class.java)
            .onFailure { return it.reason.asValidationFailure() }

        checkLot(params, pn).doOnError { return it.asValidationFailure() }
        checkItems(params).doOnError { return it.asValidationFailure() }
        checkProcurementMethodModalities(params).doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLot(
        params: ValidateRfqDataParams,
        pn: PNEntity
    ): ValidationResult<CommandValidationErrors> {
        if (params.tender.lots.size != 1)
            return CommandValidationErrors.ValidateRfqData.InvalidNumberOfLots(params.tender.lots.size)
                .asValidationFailure()

        val lot = params.tender.lots.first()

        checkLotCurrency(lot, pn).doOnError { return it.asValidationFailure() }
        checkLotContractPeriod(lot, params).doOnError { return it.asValidationFailure() }

        return ValidationResult.ok()
    }

    private fun checkLotContractPeriod(
        lot: ValidateRfqDataParams.Tender.Lot,
        params: ValidateRfqDataParams
    ): ValidationResult<CommandValidationErrors> {
        if (!lot.contractPeriod.endDate.isAfter(lot.contractPeriod.startDate))
            return CommandValidationErrors.ValidateRfqData.InvalidContractPeriod().asValidationFailure()

        if (!lot.contractPeriod.startDate.isAfter(params.tender.tenderPeriod.endDate))
            return CommandValidationErrors.ValidateRfqData.InvalidTenderPeriod().asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkLotCurrency(
        lot: ValidateRfqDataParams.Tender.Lot,
        pn: PNEntity
    ): ValidationResult<CommandValidationErrors.ValidateRfqData.InvalidCurrency> {
        val requestCurrency = lot.value.currency
        val storedCurrency = pn.tender.value.currency

        if (requestCurrency != storedCurrency)
            return CommandValidationErrors.ValidateRfqData.InvalidCurrency(
                expectedCurrency = storedCurrency, receivedCurrency = requestCurrency
            ).asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkItems(params: ValidateRfqDataParams): ValidationResult<CommandValidationErrors> {
        val lot = params.tender.lots.first()

        val duplicateItemId = params.tender.items.getDuplicate { it.id }
        if (duplicateItemId != null)
            return CommandValidationErrors.ValidateRfqData.DuplicatedItemId(duplicateItemId.id).asValidationFailure()

        val itemWithUnknownLot = params.tender.items.firstOrNull { it.relatedLot != lot.id }
        if (itemWithUnknownLot != null)
            return CommandValidationErrors.ValidateRfqData.UnknownRelatedLot(
                itemId = itemWithUnknownLot.id, relatedLot = itemWithUnknownLot.relatedLot
            ).asValidationFailure()

        return ValidationResult.ok()
    }

    private fun checkProcurementMethodModalities(params: ValidateRfqDataParams): ValidationResult<CommandValidationErrors> {
        val procurementMethodModalities = params.tender.procurementMethodModalities
        val electronicAuctions = params.tender.electronicAuctions

        when (procurementMethodModalities.contains(ProcurementMethodModalities.ELECTRONIC_AUCTION)) {
            true -> if (electronicAuctions == null)
                return CommandValidationErrors.ValidateRfqData.ElectronicAuctionsAreMissing().asValidationFailure()
            false -> if (electronicAuctions != null)
                return CommandValidationErrors.ValidateRfqData.RedundantElectronicAuctions().asValidationFailure()
        }

        return ValidationResult.ok()
    }

    override fun createRfq(params: CreateRfqParams): Result<CreateRfqResult, Fail> {
        val lotsByOldIds = params.tender.lots.associateBy(
            keySelector = { it.id },
            valueTransform = { generateLot(it) })
        val newLotIdsByOldLotIds = lotsByOldIds.mapValues { it.value.id }

        val lots = lotsByOldIds.values.toList()
        val items = generateItems(params, newLotIdsByOldLotIds)
        val rfqOcid = Ocid.SingleStage.generate(params.cpid, Stage.RQ, nowDefaultUTC())
        val relatedProcesses = generateRelatedProcess(params)

        val createdRfq = RfqEntity(
            ocid = rfqOcid,
            tender = RfqEntity.Tender(
                id = generationService.generatePermanentTenderId(),
                status = TenderStatus.ACTIVE,
                statusDetails = TenderStatusDetails.TENDERING,
                date = params.date,
                awardCriteria = AwardCriteria.PRICE_ONLY,
                awardCriteriaDetails = AwardCriteriaDetails.AUTOMATED,
                lots = lots,
                items = items,
                procurementMethodModalities = params.tender.procurementMethodModalities
            ),
            relatedProcesses = relatedProcesses,
            token = generationService.generateToken()
        )

        val entity = TenderProcessEntity(
            cpId = params.cpid.toString(),
            token = createdRfq.token,
            owner = params.owner.toString(),
            createdDate = nowDefaultUTC(),
            stage = rfqOcid.stage.key,
            jsonData = transform.trySerialization(createdRfq).onFailure { return it }

        )
        tenderProcessRepository.save(entity)
            .onFailure { return it }

        val electronicAuctions = generateElectronicAuctions(params, newLotIdsByOldLotIds)
        return generateCreateRfqResult(createdRfq, electronicAuctions).asSuccess()
    }

    private fun generateCreateRfqResult(createdRfq: RfqEntity, electronicAuctions: CreateRfqResult.Tender.ElectronicAuctions?) =
        CreateRfqResult(
            ocid = createdRfq.ocid,
            token = createdRfq.token,
            relatedProcesses = createdRfq.relatedProcesses.map { relatedProcess ->
                CreateRfqResult.RelatedProcess(
                    id = relatedProcess.id,
                    uri = relatedProcess.uri,
                    identifier = relatedProcess.identifier,
                    scheme = relatedProcess.scheme,
                    relationship = relatedProcess.relationship
                )
            },
            tender = createdRfq.tender.let { tender ->
                CreateRfqResult.Tender(
                    id = tender.id,
                    electronicAuctions = electronicAuctions,
                    procurementMethodModalities = tender.procurementMethodModalities,
                    items = tender.items
                        .map { item ->
                            CreateRfqResult.Tender.Item(
                                id = item.id,
                                relatedLot = item.relatedLot,
                                description = item.description,
                                classification = item.classification.let { classification ->
                                    CreateRfqResult.Tender.Item.Classification(
                                        id = classification.id,
                                        description = classification.description,
                                        scheme = classification.scheme
                                    )
                                },
                                quantity = item.quantity,
                                unit = item.unit.let { unit ->
                                    CreateRfqResult.Tender.Item.Unit(
                                        id = unit.id,
                                        name = unit.name
                                    )
                                },
                                internalId = item.internalId
                            )
                        },
                    lots = tender.lots.map { lot ->
                        CreateRfqResult.Tender.Lot(
                            id = lot.id,
                            internalId = lot.internalId,
                            description = lot.description,
                            value = CreateRfqResult.Tender.Lot.Value(lot.value.currency),
                            contractPeriod = lot.contractPeriod.let { contractPeriod ->
                                CreateRfqResult.Tender.Lot.ContractPeriod(
                                    startDate = contractPeriod.startDate,
                                    endDate = contractPeriod.endDate
                                )
                            },
                            title = lot.title,
                            statusDetails = lot.statusDetails,
                            status = lot.status,
                            placeOfPerformance = lot.placeOfPerformance.let { placeOfPerformance ->
                                CreateRfqResult.Tender.Lot.PlaceOfPerformance(
                                    description = placeOfPerformance.description,
                                    address = placeOfPerformance.address
                                        .let { address ->
                                            CreateRfqResult.Tender.Lot.PlaceOfPerformance.Address(
                                                streetAddress = address.streetAddress,
                                                postalCode = address.postalCode,
                                                addressDetails = address.addressDetails.let { addressDetails ->
                                                    CreateRfqResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                                        country = addressDetails.country.let { country ->
                                                            CreateRfqResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                                                scheme = country.scheme,
                                                                id = country.id,
                                                                description = country.description,
                                                                uri = country.uri
                                                            )
                                                        },
                                                        region = addressDetails.region.let { region ->
                                                            CreateRfqResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                                                scheme = region.scheme,
                                                                id = region.id,
                                                                description = region.description,
                                                                uri = region.uri
                                                            )
                                                        },
                                                        locality = addressDetails.locality.let { locality ->
                                                            CreateRfqResult.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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
                    status = tender.status,
                    statusDetails = tender.statusDetails,
                    awardCriteriaDetails = tender.awardCriteriaDetails,
                    awardCriteria = tender.awardCriteria,
                    date = tender.date
                )
            }
        )

    private fun generateRelatedProcess(params: CreateRfqParams): List<RfqEntity.RelatedProcess> {
        val msRelation = RfqEntity.RelatedProcess(
            id = generationService.relatedProcessId(),
            relationship = listOf(RelatedProcessType.PARENT),
            scheme = "ocid",
            identifier = params.cpid.toString(),
            uri = "${uriProperties.tender}/${params.cpid}/${params.cpid}"
        )

        val pcrRelation = RfqEntity.RelatedProcess(
            id = generationService.relatedProcessId(),
            relationship = listOf(RelatedProcessType.X_CATALOGUE),
            scheme = "ocid",
            identifier = params.additionalOcid.toString(),
            uri = "${uriProperties.tender}/${params.additionalCpid}/${params.additionalOcid}"
        )
        return listOf(msRelation, pcrRelation)
    }

    private fun generateElectronicAuctions(
        params: CreateRfqParams,
        newLotIdsByOldLotIds: Map<String, LotId>
    ) = params.tender.electronicAuctions
        ?.let { electronicAuctions ->
            CreateRfqResult.Tender.ElectronicAuctions(
                details = electronicAuctions.details
                    .map { detail ->
                        CreateRfqResult.Tender.ElectronicAuctions.Detail(
                            id = detail.id,
                            relatedLot = newLotIdsByOldLotIds.getValue(detail.relatedLot)
                        )
                    })
        }

    private fun generateLot(lot: CreateRfqParams.Tender.Lot) = RfqEntity.Tender.Lot(
        id = generationService.lotId(),
        status = LotStatus.ACTIVE,
        statusDetails = LotStatusDetails.EMPTY,
        description = lot.description,
        internalId = lot.internalId,
        title = lot.title,
        placeOfPerformance = RfqEntity.Tender.Lot.PlaceOfPerformance(
            description = lot.placeOfPerformance.description,
            address = lot.placeOfPerformance.address
                .let { address ->
                    RfqEntity.Tender.Lot.PlaceOfPerformance.Address(
                        streetAddress = address.streetAddress,
                        postalCode = address.postalCode,
                        addressDetails = address.addressDetails.let { addressDetails ->
                            RfqEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails(
                                country = addressDetails.country.let { country ->
                                    RfqEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Country(
                                        scheme = country.scheme,
                                        id = country.id,
                                        description = country.description,
                                        uri = country.uri
                                    )
                                },
                                region = addressDetails.region.let { region ->
                                    RfqEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Region(
                                        scheme = region.scheme,
                                        id = region.id,
                                        description = region.description,
                                        uri = region.uri
                                    )
                                },
                                locality = addressDetails.locality.let { locality ->
                                    RfqEntity.Tender.Lot.PlaceOfPerformance.Address.AddressDetails.Locality(
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

        ),
        contractPeriod = lot.contractPeriod.let { contractPeriod ->
            RfqEntity.Tender.Lot.ContractPeriod(
                startDate = contractPeriod.startDate,
                endDate = contractPeriod.endDate
            )
        },
        value = RfqEntity.Tender.Lot.Value(currency = lot.value.currency)
    )

    private fun generateItems(
        params: CreateRfqParams,
        newLotIdsByOldLotIds: Map<String, LotId>
    ) = params.tender.items
        .map { item ->
            RfqEntity.Tender.Item(
                id = generationService.generatePermanentItemId(),
                internalId = item.internalId,
                description = item.description,
                relatedLot = newLotIdsByOldLotIds.getValue(item.relatedLot),
                unit = RfqEntity.Tender.Item.Unit(id = item.unit.id, name = item.unit.name),
                quantity = item.quantity,
                classification = item.classification.let { classification ->
                    RfqEntity.Tender.Item.Classification(
                        id = classification.id,
                        description = classification.description,
                        scheme = classification.scheme
                    )
                }
            )
        }

    override fun createRelationToContractProcessStage(params: CreateRelationToContractProcessStageParams)
        : Result<CreateRelationToContractProcessStageResult, Fail> =
        CreateRelationToContractProcessStageResult(
            relatedProcesses = listOf(
                CreateRelationToContractProcessStageResult.RelatedProcess(
                    id = generationService.relatedProcessId(),
                    relationship = getRelationship(params),
                    scheme = "ocid",
                    identifier = params.relatedOcid.toString(),
                    uri = "${uriProperties.tender}/${params.cpid}/${params.relatedOcid}"
                )
            )
        ).asSuccess()

    private fun getRelationship(params: CreateRelationToContractProcessStageParams) =
        when (params.operationType) {
            OperationType.CREATE_RFQ -> listOf(RelatedProcessType.X_PURCHASING)
            OperationType.UPDATE_AWARD,
            OperationType.SUBMIT_BID,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.DIVIDE_LOT,
            OperationType.CREATE_AWARD,
            OperationType.AWARD_CONSIDERATION,
            OperationType.AMEND_FE,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> throw ErrorException(ErrorType.INVALID_OPERATION_TYPE)
        }
}






