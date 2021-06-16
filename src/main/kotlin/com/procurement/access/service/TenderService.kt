package com.procurement.access.service


import com.procurement.access.application.model.errors.DefineTenderClassificationErrors
import com.procurement.access.application.model.errors.GetBuyersOwnersErrors
import com.procurement.access.application.model.params.DefineTenderClassificationParams
import com.procurement.access.application.model.params.FindAuctionsParams
import com.procurement.access.application.model.params.GetBuyersOwnersParams
import com.procurement.access.application.model.params.GetCurrencyParams
import com.procurement.access.application.model.params.GetMainProcurementCategoryParams
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsErrors
import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsParams
import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsResult
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateParams
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateResult
import com.procurement.access.dao.TenderProcessDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import com.procurement.access.domain.model.enums.RelatedProcessType
import com.procurement.access.domain.model.enums.Scheme
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.toCPVCode
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType.CONTEXT
import com.procurement.access.exception.ErrorType.DATA_NOT_FOUND
import com.procurement.access.exception.ErrorType.INVALID_LOTS_STATUS
import com.procurement.access.exception.ErrorType.INVALID_OPERATION_TYPE
import com.procurement.access.exception.ErrorType.INVALID_OWNER
import com.procurement.access.exception.ErrorType.INVALID_STAGE
import com.procurement.access.exception.ErrorType.INVALID_TOKEN
import com.procurement.access.exception.ErrorType.IS_NOT_SUSPENDED
import com.procurement.access.exception.ErrorType.TENDER_IN_UNSUCCESSFUL_STATUS
import com.procurement.access.infrastructure.api.v1.ApiResponseV1
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.commandId
import com.procurement.access.infrastructure.api.v1.stage
import com.procurement.access.infrastructure.entity.APEntity
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.FEEntity
import com.procurement.access.infrastructure.entity.PNEntity
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.infrastructure.entity.TenderCategoryInfo
import com.procurement.access.infrastructure.entity.TenderCurrencyInfo
import com.procurement.access.infrastructure.entity.TenderStateInfo
import com.procurement.access.infrastructure.handler.v1.model.request.CancellationRs
import com.procurement.access.infrastructure.handler.v1.model.request.GetDataForAcRq
import com.procurement.access.infrastructure.handler.v1.model.request.GetDataForAcRs
import com.procurement.access.infrastructure.handler.v1.model.request.GetDataForAcTender
import com.procurement.access.infrastructure.handler.v1.model.request.LotCancellation
import com.procurement.access.infrastructure.handler.v1.model.response.GetTenderOwnerRs
import com.procurement.access.infrastructure.handler.v1.model.response.UnsuspendedTender
import com.procurement.access.infrastructure.handler.v1.model.response.UnsuspendedTenderRs
import com.procurement.access.infrastructure.handler.v1.model.response.UpdateTenderStatusRs
import com.procurement.access.infrastructure.handler.v2.model.response.DefineTenderClassificationResult
import com.procurement.access.infrastructure.handler.v2.model.response.FindAuctionsResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetBuyersOwnersResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetCurrencyResult
import com.procurement.access.infrastructure.handler.v2.model.response.GetMainProcurementCategoryResult
import com.procurement.access.infrastructure.handler.v2.model.response.from
import com.procurement.access.lib.extension.toSet
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.TenderProcess
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toJson
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class TenderService(
    private val tenderProcessDao: TenderProcessDao,
    private val generationService: GenerationService,
    private val tenderProcessRepository: TenderProcessRepository,
    private val transform: Transform
) {

    companion object {
        private const val ITEMS_CATEGORY_LENGTH_MIN = 3
        private const val ITEMS_CATEGORY_LENGTH_MAX = 4
    }

    fun setSuspended(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        process.tender.statusDetails = TenderStatusDetails.SUSPENDED
        tenderProcessDao.save(getEntity(process, entity))
        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = UpdateTenderStatusRs(
                process.tender.status.key,
                process.tender.statusDetails.key
            )
        )
    }

    fun setUnsuspended(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)

        val result = when (Stage.creator(stage)) {

            Stage.FE -> {
                val process = toObject(FEEntity::class.java, entity.jsonData)
                    .let { fe ->
                        if (fe.tender.statusDetails == TenderStatusDetails.SUSPENDED)
                            fe.copy(tender = fe.tender.copy(statusDetails = TenderStatusDetails.creator(phase)))
                        else
                            throw ErrorException(IS_NOT_SUSPENDED)
                    }

                tenderProcessDao.save(
                    TenderProcessEntity(
                        cpId = entity.cpId,
                        token = entity.token,
                        ocid = entity.ocid,
                        owner = entity.owner,
                        createdDate = nowDefaultUTC(),
                        jsonData = toJson(process)
                    )
                )

                UnsuspendedTenderRs(
                    UnsuspendedTender(
                        process.tender.status.key,
                        process.tender.statusDetails.key,
                        process.tender.procurementMethodModalities?.toSet(),
                        null
                    )
                )
            }

            Stage.EV,
            Stage.NP,
            Stage.TP -> {
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                if (process.tender.statusDetails == TenderStatusDetails.SUSPENDED)
                    process.tender.statusDetails = TenderStatusDetails.creator(phase)
                else
                    throw ErrorException(IS_NOT_SUSPENDED)

                tenderProcessDao.save(getEntity(process, entity))

                UnsuspendedTenderRs(
                    UnsuspendedTender(
                        process.tender.status.key,
                        process.tender.statusDetails.key,
                        process.tender.procurementMethodModalities,
                        process.tender.electronicAuctions
                    )
                )
            }

            Stage.AC,
            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN,
            Stage.RQ ->
                throw ErrorException(INVALID_STAGE)
        }

        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = result)
    }

    fun setCancellation(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val owner = cm.context.owner ?: throw ErrorException(CONTEXT)
        val token = cm.context.token ?: throw ErrorException(CONTEXT)
        val operationType = cm.context.operationType ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        if (entity.owner != owner) throw ErrorException(INVALID_OWNER)
        if (entity.token.toString() != token) throw ErrorException(INVALID_TOKEN)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        validateTenderStatusForCancellation(process, operationType)
        val lotStatusPredicate = getLotStatusPredicateForCancellation(operationType)
        val lotsResponseDto = mutableListOf<LotCancellation>()
        process.tender.apply {
            status = TenderStatus.CANCELLED
            statusDetails = TenderStatusDetails.EMPTY
            lots.asSequence()
                    .filter(lotStatusPredicate)
                    .forEach { lot ->
                        lot.status = LotStatus.CANCELLED
                        lot.statusDetails = LotStatusDetails.EMPTY
                        addLotToLotsResponseDto(lotsResponseDto, lot)
                    }
        }
        tenderProcessDao.save(getEntity(process, entity))
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = CancellationRs(lots = lotsResponseDto))
    }

    fun setStatusDetails(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val phase = cm.context.phase ?: throw ErrorException(CONTEXT)
        val stage = cm.stage

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage.key) ?: throw ErrorException(DATA_NOT_FOUND)

        val result = when (stage) {
            Stage.AC,
            Stage.EV,
            Stage.FE,
            Stage.NP,
            Stage.TP -> {
                val process = toObject(TenderProcess::class.java, entity.jsonData)
                process.tender.statusDetails = TenderStatusDetails.creator(phase)
                tenderProcessDao.save(getEntity(process, entity))
                UpdateTenderStatusRs(
                    process.tender.status.key,
                    process.tender.statusDetails.key
                )
            }

            Stage.RQ -> {
                val rfq = toObject(RfqEntity::class.java, entity.jsonData)
                val updatedRfq = rfq.copy(tender = rfq.tender.copy(statusDetails = TenderStatusDetails.creator(phase)))
                val updatedRfqEntity = entity.copy(
                    createdDate = nowDefaultUTC(),
                    jsonData = toJson(updatedRfq)
                )
                tenderProcessDao.save(updatedRfqEntity)
                UpdateTenderStatusRs(
                    updatedRfq.tender.status.key,
                    updatedRfq.tender.statusDetails.key
                )
            }

            Stage.AP,
            Stage.EI,
            Stage.FS,
            Stage.PC,
            Stage.PN -> throw ErrorException(
                error = INVALID_STAGE,
                message = "Stage ${stage} not allowed at the command."
            )
        }

        return ApiResponseV1.Success(
            version = cm.version,
            id = cm.commandId,
            data = result
        )
    }

    fun getTenderOwner(cm: CommandMessage): ApiResponseV1.Success {
        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = GetTenderOwnerRs(entity.owner))
    }

    fun getDataForAc(cm: CommandMessage): ApiResponseV1.Success {

        val cpId = cm.context.cpid ?: throw ErrorException(CONTEXT)
        val stage = cm.context.stage ?: throw ErrorException(CONTEXT)
        val dto = toObject(GetDataForAcRq::class.java, cm.data)
        val lotsIdsSet = dto.awards.asSequence().map { it.relatedLots[0] }.toSet()

        val entity = tenderProcessDao.getByCpIdAndStage(cpId, stage) ?: throw ErrorException(DATA_NOT_FOUND)
        val process = toObject(TenderProcess::class.java, entity.jsonData)
        val lots = process.tender.lots.asSequence().filter { lotsIdsSet.contains(it.id) }.toList()
        if (lots.asSequence().any { it.status == LotStatus.CANCELLED || it.status == LotStatus.UNSUCCESSFUL }) {
            throw ErrorException(INVALID_LOTS_STATUS)
        }
        val items = process.tender.items.asSequence().filter { lotsIdsSet.contains(it.relatedLot) }.toList()
        val contractedTender = GetDataForAcTender(
                id = generationService.generatePermanentTenderId(),
                classification = process.tender.classification,
                procurementMethod = process.tender.procurementMethod,
                procurementMethodDetails = process.tender.procurementMethodDetails,
                mainProcurementCategory = process.tender.mainProcurementCategory,
                lots = lots,
                items = items)
        return ApiResponseV1.Success(version = cm.version, id = cm.commandId, data = GetDataForAcRs(contractedTender))
    }

    private fun getLotStatusPredicateForPrepareCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancellationStandstillPeriod" -> { lot: Lot ->
                (lot.status == LotStatus.ACTIVE)
                        && (lot.statusDetails == LotStatusDetails.EMPTY || lot.statusDetails == LotStatusDetails.AWARDED)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == LotStatus.PLANNING || lot.status == LotStatus.PLANNED)
                        && (lot.statusDetails == LotStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun getLotStatusPredicateForCancellation(operationType: String): (Lot) -> Boolean {
        return when (operationType) {
            "cancelTender", "cancelTenderEv" -> { lot: Lot ->
                (lot.status == LotStatus.ACTIVE)
            }
            "cancelPlan" -> { lot: Lot ->
                (lot.status == LotStatus.PLANNING || lot.status == LotStatus.PLANNED)
                        && (lot.statusDetails == LotStatusDetails.EMPTY)
            }
            else -> {
                throw ErrorException(INVALID_OPERATION_TYPE)
            }
        }
    }

    private fun validateTenderStatusForPrepareCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancellationStandstillPeriod" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
        }
    }

    private fun validateTenderStatusForCancellation(process: TenderProcess, operationType: String) {
        when (operationType) {
            "cancelTender", "cancelTenderEv" -> {
                if (process.tender.status != TenderStatus.ACTIVE)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
                if (process.tender.statusDetails != TenderStatusDetails.CANCELLATION)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
            "cancelPlan" -> {
                if (process.tender.status != TenderStatus.PLANNING && process.tender.status != TenderStatus.PLANNED)
                    throw ErrorException(TENDER_IN_UNSUCCESSFUL_STATUS)
            }
        }
    }

    private fun addLotToLotsResponseDto(lotsResponseDto: MutableList<LotCancellation>, lot: Lot) {
        lotsResponseDto.add(LotCancellation(
                id = lot.id,
                status = lot.status,
                statusDetails = lot.statusDetails))
    }

    private fun getEntity(process: TenderProcess,
                          entity: TenderProcessEntity): TenderProcessEntity {

        return TenderProcessEntity(
            cpId = entity.cpId,
            token = entity.token,
            ocid = entity.ocid,
            owner = entity.owner,
            createdDate = nowDefaultUTC(),
            jsonData = toJson(process)
        )
    }

    fun getTenderState(params: GetTenderStateParams): Result<GetTenderStateResult, Fail> {
        val entity = tenderProcessRepository
            .getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .onFailure { incident -> return incident }
            ?: return ValidationErrors.TenderNotFoundOnGetTenderState(cpid = params.cpid, ocid = params.ocid)
                .asFailure()

        return entity.jsonData
            .tryToObject(TenderStateInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(it.exception) }
            .onFailure { return it }
            .let {
                val tender = it.tender
                GetTenderStateResult(
                    status = tender.status,
                    statusDetails = tender.statusDetails
                )
            }
            .asSuccess()
    }

    fun getItemsByLotIds(params: GetItemsByLotIdsParams): Result<GetItemsByLotIdsResult, Fail> {
        val entity = tenderProcessRepository
            .getByCpIdAndOcid(cpid = params.cpid, ocid = params.ocid)
            .onFailure { incident -> return incident }
            ?: return GetItemsByLotIdsErrors.RecordNotFound(cpid = params.cpid, ocid = params.ocid)
                .asFailure()

        val receivedLotIds = params.tender.lots.toSet { it.id }

        val storedItems = entity.jsonData
            .tryToObject(CNEntity::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(it.exception) }
            .onFailure { return it }
            .tender.items

        val targetItems = storedItems.filter { it.relatedLot in receivedLotIds }

        val missingLots = receivedLotIds - targetItems.map { it.relatedLot }

        if (missingLots.isNotEmpty())
            return GetItemsByLotIdsErrors.ItemsNotFound(params.cpid, params.ocid, missingLots).asFailure()

        return targetItems
            .map { GetItemsByLotIdsResult.fromDomain(it) }
            .let { GetItemsByLotIdsResult(tender = GetItemsByLotIdsResult.Tender(items = it)) }
            .asSuccess()
    }

    fun findAuctions(params: FindAuctionsParams): Result<FindAuctionsResult?, Fail> {
        val entity = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { fail -> return fail }
            ?: return ValidationErrors.TenderNotFoundOnFindAuctions(params.cpid, params.ocid).asFailure()

        val tenderProcess = entity.jsonData
            .tryToObject(CNEntity::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(it.exception) }
            .onFailure { return it }

        if (tenderProcess.tender.electronicAuctions == null)
            return null.asSuccess()

        return FindAuctionsResult(
            FindAuctionsResult.Tender(
                FindAuctionsResult.Tender.ElectronicAuctions(
                    tenderProcess.tender.electronicAuctions.details
                        .map { detail ->
                            FindAuctionsResult.Tender.ElectronicAuctions.Detail(
                                id = detail.id,
                                relatedLot = detail.relatedLot,
                                electronicAuctionModalities = detail.electronicAuctionModalities
                                    .map { modality ->
                                        FindAuctionsResult.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality(
                                            modality.eligibleMinimumDifference.let { eligibleMinimumDifference ->
                                                FindAuctionsResult.Tender.ElectronicAuctions.Detail.ElectronicAuctionModality.EligibleMinimumDifference(
                                                    amount = eligibleMinimumDifference.amount,
                                                    currency = eligibleMinimumDifference.currency
                                                )

                                            }
                                        )
                                    }
                            )
                        }
                )
            )
        ).asSuccess()
    }

    fun getCurrency(params: GetCurrencyParams): Result<GetCurrencyResult, Fail> {
        val record = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { fail -> return fail }
            ?: return failure(
                ValidationErrors.TenderNotFoundOnGetCurrency(params.cpid, params.ocid)
            )

        val tenderInfo = record.jsonData.tryToObject(TenderCurrencyInfo::class.java)
            .onFailure { fail -> return fail }

        return GetCurrencyResult(GetCurrencyResult.Tender(GetCurrencyResult.Tender.Value(tenderInfo.tender.value.currency))).asSuccess()
    }

    fun getMainProcurementCategory(params: GetMainProcurementCategoryParams): Result<GetMainProcurementCategoryResult, Fail> {
        val tenderEntity = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { fail -> return fail }
            ?: return failure(ValidationErrors.TenderNotFoundOnGetMainProcurementCategory(params.cpid, params.ocid))

        val tenderCategory = tenderEntity.jsonData
            .tryToObject(TenderCategoryInfo::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(it.exception) }
            .onFailure { return it }

        return GetMainProcurementCategoryResult(tender = GetMainProcurementCategoryResult.Tender(tenderCategory.tender.mainProcurementCategory)).asSuccess()
    }

    fun getBuyersOwners(params: GetBuyersOwnersParams): Result<GetBuyersOwnersResult, Fail> {
        val fe = tenderProcessRepository.getByCpIdAndOcid(params.cpid, params.ocid)
            .onFailure { fail -> return fail }
            ?.let { transform.tryDeserialization(it.jsonData, FEEntity::class.java) }
            ?.onFailure { return it }
            ?: return GetBuyersOwnersErrors.FeRecordNotFound(params.cpid, params.ocid).asFailure()

        val apOcid = fe.relatedProcesses
            ?.firstOrNull { it.relationship.contains(RelatedProcessType.AGGREGATE_PLANNING) }
            ?.identifier
            ?.let { Ocid.SingleStage.tryCreateOrNull(it.value)!! }
            ?: return GetBuyersOwnersErrors.MissingAggregatePlanningRelationship().asFailure()

        val ap = tenderProcessRepository.getByCpIdAndOcid(params.cpid, apOcid)
            .onFailure { fail -> return fail }
            ?.let { transform.tryDeserialization(it.jsonData, APEntity::class.java) }
            ?.onFailure { return it }
            ?: return GetBuyersOwnersErrors.ApRecordNotFound(params.cpid, apOcid).asFailure()

        val pnOcids = ap.relatedProcesses
            .orEmpty()
            .filter { it.relationship.contains(RelatedProcessType.X_SCOPE) }
            .map { Ocid.SingleStage.tryCreateOrNull(it.identifier.value)!! }

        if (pnOcids.isEmpty())
            return GetBuyersOwnersErrors.MissingXScopeRelationship().asFailure()

        val pnEntities = pnOcids.map { ocid ->
            val cpid = ocid.extractCpid()
            tenderProcessRepository.getByCpIdAndOcid(cpid, ocid)
                .onFailure { fail -> return fail }
                ?: return GetBuyersOwnersErrors.PnRecordNotFound(cpid, ocid).asFailure()
        }

        val entitiesByPn = pnEntities.associateBy {
            transform.tryDeserialization(it.jsonData, PNEntity::class.java)
                .onFailure { return it }
        }

        return GetBuyersOwnersResult(buyers = entitiesByPn.mapNotNull { pn ->
            pn.key.buyer?.let { buyer ->
                GetBuyersOwnersResult.Buyer(
                    id = buyer.id,
                    name = buyer.name,
                    owner = pn.value.owner
                )
            }
        }).asSuccess()
    }

    fun defineTenderClassification(params: DefineTenderClassificationParams): Result<DefineTenderClassificationResult, Fail> {
        val isHomogeneous = isIdHomogeneous(params.tender.items) // FR.COM-1.51.1

        val homogeneousItems =
            if (!isHomogeneous) {
                // FR.COM-1.51.2
                val pnEntity = tenderProcessRepository
                    .getByCpIdAndOcid(params.relatedCpid, params.relatedOcid).onFailure { return it }
                    ?: return DefineTenderClassificationErrors.RecordNotFound(params.relatedCpid, params.relatedOcid).asFailure()

                val pn = transform.tryDeserialization(pnEntity.jsonData, PNEntity::class.java)
                    .onFailure { return it }

                val pnItemsCategory = pn.tender.classification.id.take(ITEMS_CATEGORY_LENGTH_MIN)
                params.tender.items.filter { it.classification.id.startsWith(pnItemsCategory) }
            } else {
                params.tender.items
            }

        if (homogeneousItems.isEmpty())
            return DefineTenderClassificationErrors.NoHomogeneousItems().asFailure()

        val commonCategory = defineCommonCategory(homogeneousItems) // FR.COM-1.51.4

        val definedClassificationId = commonCategory.toCPVCode() // FR.COM-1.51.4
        val definedClassificationScheme = getSchemeIfHomogeneous(homogeneousItems).onFailure { return it } // FR.COM-1.51.5

       return DefineTenderClassificationResult.from(definedClassificationId, definedClassificationScheme).asSuccess()
    }

    private fun isIdHomogeneous(items: List<DefineTenderClassificationParams.Tender.Item>): Boolean {
        val uniqCategories = items.toSet { it.classification.id.take(ITEMS_CATEGORY_LENGTH_MIN) }
        return uniqCategories.size == 1
    }

    private fun isSchemeHomogeneous(items: List<DefineTenderClassificationParams.Tender.Item>): Boolean {
        val uniqSchemes = items.toSet { it.classification.scheme }
        return uniqSchemes.size == 1
    }

    private fun getSchemeIfHomogeneous(items: List<DefineTenderClassificationParams.Tender.Item>): Result<Scheme, Fail> =
        if (isSchemeHomogeneous(items))
            items.first().classification.scheme.asSuccess()
        else
            DefineTenderClassificationErrors.MultiScheme().asFailure()

    private fun defineCommonCategory(items: List<DefineTenderClassificationParams.Tender.Item>): String {
        var commonCategory = ""
        for (categoryLength in ITEMS_CATEGORY_LENGTH_MIN .. ITEMS_CATEGORY_LENGTH_MAX) {
            val uniqCategories = items.toSet { it.classification.id.take(categoryLength) }
            if (uniqCategories.size == 1)
                commonCategory = uniqCategories.first()
        }
        return commonCategory
    }

}
