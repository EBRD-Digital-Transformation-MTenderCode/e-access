package com.procurement.access.infrastructure.handler.set.stateforlots

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import org.springframework.stereotype.Service

@Service
class SetStateForLotsHandler(
    private val lotService: LotService,
    private val historyDao: HistoryDao,
    private val logger: Logger
) : AbstractHistoricalHandler<Command2Type, List<SetStateForLotsResult>>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<List<SetStateForLotsResult>, Fail> {

        val params = node.tryGetParams()
            .bind { it.tryParamsToObject(SetStateForLotsRequest::class.java) }
            .bind { it.convert() }
            .orForwardFail { error -> return error }

        return lotService.setStateForLots(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.SET_STATE_FOR_LOTS
}
