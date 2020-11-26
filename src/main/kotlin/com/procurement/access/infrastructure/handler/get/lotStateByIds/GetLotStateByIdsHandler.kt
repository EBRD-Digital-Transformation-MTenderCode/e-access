package com.procurement.access.infrastructure.handler.get.lotStateByIds

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import org.springframework.stereotype.Service

@Service
class GetLotStateByIdsHandler(
    private val logger: Logger,
    private val historyDao: HistoryDao,
    private val lotService: LotService
) : AbstractHistoricalHandler<Command2Type, List<GetLotStateByIdsResult>>(
    target = ApiSuccessResponse::class.java,
    historyRepository = historyDao,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<List<GetLotStateByIdsResult>, Fail> {
        val paramsNode = node.tryGetParams()
            .doOnError { error -> return Result.failure(error) }
            .get
        val params = paramsNode.tryParamsToObject(GetLotStateByIdsRequest::class.java)
            .doOnError { error -> return Result.failure(error) }
            .get
            .convert()
            .doOnError { error -> return Result.failure(error) }
            .get
        return lotService.getLotStateByIds(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.GET_LOT_STATE_BY_IDS
}
