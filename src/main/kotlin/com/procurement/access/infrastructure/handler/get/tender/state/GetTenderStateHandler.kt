package com.procurement.access.infrastructure.handler.get.tender.state

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateResult
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractQueryHandler
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Component

@Component
class GetTenderStateHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandler<Command2Type, GetTenderStateResult>(
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetTenderStateResult, Fail> {
        val params = node.tryGetParams()
            .forwardResult { error -> return error }
            .tryParamsToObject(GetTenderStateRequest::class.java)
            .forwardResult { error -> return error }
            .convert()
            .forwardResult { error -> return error }

        return tenderService.getTenderState(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.GET_TENDER_STATE
}