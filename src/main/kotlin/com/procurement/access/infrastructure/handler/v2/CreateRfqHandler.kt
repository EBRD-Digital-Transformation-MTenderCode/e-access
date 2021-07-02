package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRfqRequest
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRfqResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.RfqService
import org.springframework.stereotype.Service

@Service
class CreateRfqHandler(
    transform: Transform,
    private val rfqService: RfqService,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<CreateRfqResult>(transform, historyRepositoryOld, historyRepositoryNew, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.CREATE_RFQ

    override fun execute(descriptor: CommandDescriptor): Result<CreateRfqResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<CreateRfqRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return rfqService.createRfq(params = params)
    }
}
