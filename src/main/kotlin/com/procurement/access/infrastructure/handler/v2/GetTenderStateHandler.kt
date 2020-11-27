package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateResult
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractQueryHandlerV2
import com.procurement.access.infrastructure.handler.v2.model.request.GetTenderStateRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Component

@Component
class GetTenderStateHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandlerV2<GetTenderStateResult>(logger = logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_TENDER_STATE

    override fun execute(descriptor: CommandDescriptor): Result<GetTenderStateResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetTenderStateRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.getTenderState(params = params)
    }
}