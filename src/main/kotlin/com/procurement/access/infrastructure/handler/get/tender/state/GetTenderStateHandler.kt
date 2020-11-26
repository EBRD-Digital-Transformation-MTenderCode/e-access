package com.procurement.access.infrastructure.handler.get.tender.state

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateResult
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractQueryHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Component

@Component
class GetTenderStateHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandler<CommandTypeV2, GetTenderStateResult>(
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetTenderStateResult, Fail> {
        val params = node.tryGetParams()
            .onFailure { error -> return error }
            .tryParamsToObject(GetTenderStateRequest::class.java)
            .onFailure { error -> return error }
            .convert()
            .onFailure { error -> return error }

        return tenderService.getTenderState(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_TENDER_STATE
}