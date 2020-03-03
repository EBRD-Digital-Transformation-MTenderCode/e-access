package com.procurement.access.infrastructure.handler.get.lotids

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.lot.GetLotIdsParams
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
import org.springframework.stereotype.Service

@Service
class GetLotIdsHandler(
    private val lotService: LotService,
    private val historyDao: HistoryDao
) : AbstractHistoricalHandler<Command2Type, List<LotId>>(historyRepository = historyDao,target = ApiResponse::class.java) {

    override fun execute(node: JsonNode): Result<List<LotId>, List<Fail>> {
        val params =
            when (val paramsResult = node.tryGetParams(GetLotIdsRequest::class.java)) {
                is Result.Success -> paramsResult.get
                is Result.Failure -> return Result.failure(listOf(paramsResult.error))

            }
        val data = when (val result:Result<GetLotIdsParams, List<DataErrors>> = params.convert()) {
            is Result.Success -> result.get
            is Result.Failure -> return result
        }
        val stage = data.ocid.split("-")[4]

        val resultService = lotService.getLotIds(cpId = data.cpid, stage = stage, states = data.states)
        if(resultService.isFail)
            return Result.failure(listOf(resultService.error))

        return Result.success(resultService.get)
    }

    override val action: Command2Type
        get() = Command2Type.GET_LOT_IDS
}
