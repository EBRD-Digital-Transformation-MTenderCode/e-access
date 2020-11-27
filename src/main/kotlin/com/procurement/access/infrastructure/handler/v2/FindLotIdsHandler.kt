package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.FindLotIdsRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import org.springframework.stereotype.Service

@Service
class FindLotIdsHandler(
    private val lotService: LotService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<List<LotId>>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.FIND_LOT_IDS

    override fun execute(descriptor: CommandDescriptor): Result<List<LotId>, Fail> {
        val params = descriptor.body.asJsonNode
            .params<FindLotIdsRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return lotService.findLotIds(params = params)
    }
}
