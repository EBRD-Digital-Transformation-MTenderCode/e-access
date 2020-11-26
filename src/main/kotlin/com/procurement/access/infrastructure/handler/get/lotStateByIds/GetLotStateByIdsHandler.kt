package com.procurement.access.infrastructure.handler.get.lotStateByIds

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import org.springframework.stereotype.Service

@Service
class GetLotStateByIdsHandler(
    private val logger: Logger,
    private val historyDao: HistoryDao,
    private val lotService: LotService
) : AbstractHistoricalHandler<CommandTypeV2, List<GetLotStateByIdsResult>>(
    target = ApiResponseV2.Success::class.java,
    historyRepository = historyDao,
    logger = logger
) {
    override fun execute(node: JsonNode): Result<List<GetLotStateByIdsResult>, Fail> {
        val paramsNode = node.tryGetParams()
            .onFailure { return it }
        val params = paramsNode.tryParamsToObject(GetLotStateByIdsRequest::class.java)
            .onFailure { return it }
            .convert()
            .onFailure { return it }
        return lotService.getLotStateByIds(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_LOT_STATE_BY_IDS
}
