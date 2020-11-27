package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.GetLotStateByIdsRequest
import com.procurement.access.infrastructure.handler.v2.model.response.GetLotStateByIdsResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import org.springframework.stereotype.Service

@Service
class GetLotStateByIdsHandler(
    transform: Transform,
    logger: Logger,
    historyDao: HistoryDao,
    private val lotService: LotService
) : AbstractHistoricalHandler<List<GetLotStateByIdsResult>>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_LOT_STATE_BY_IDS

    override fun execute(descriptor: CommandDescriptor): Result<List<GetLotStateByIdsResult>, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetLotStateByIdsRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return lotService.getLotStateByIds(params = params)
    }
}
