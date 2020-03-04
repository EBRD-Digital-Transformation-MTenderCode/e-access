package com.procurement.access.infrastructure.handler.get.lotids

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.utils.getStageFromOcid
import org.springframework.stereotype.Service

@Service
class GetLotIdsHandler(
    private val lotService: LotService,
    private val historyDao: HistoryDao
) : AbstractHistoricalHandler<Command2Type, List<LotId>>(
    historyRepository = historyDao,
    target = ApiResponse::class.java
) {

    override fun execute(node: JsonNode): Result<List<LotId>, List<Fail>> {
        val params = node.tryGetParams(GetLotIdsRequest::class.java)
            .doOnError { error: DataErrors -> return Result.failure(listOf(error)) }
            .get
            .convert()
            .doOnError { error -> return Result.failure(error) }
            .get

        val stage = params.ocid.getStageFromOcid()

        return lotService.getLotIds(cpId = params.cpid, stage = stage, states = params.states)
            .mapError { fail -> listOf(fail) }
    }

    override val action: Command2Type
        get() = Command2Type.GET_LOT_IDS
}
