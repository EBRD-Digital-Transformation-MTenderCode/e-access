package com.procurement.access.infrastructure.handler.pn

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.OutsourcingService
import org.springframework.stereotype.Service

@Service
class OutsourcingPNHandler(
    private val outsourcingService: OutsourcingService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, OutsourcingPNResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<OutsourcingPNResult, Fail> {
        val params = node.tryGetParams()
            .flatMap { it.tryParamsToObject(OutsourcingPNRequest::class.java) }
            .flatMap { it.convert() }
            .onFailure { error -> return error }

        return outsourcingService.outsourcingPN(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.OUTSOURCING_PN
}
