package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.ResponderProcessingRequest
import com.procurement.access.infrastructure.handler.v2.model.response.ResponderProcessingResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class ResponderProcessingHandler(
    private val responderService: ResponderService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<ResponderProcessingResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.RESPONDER_PROCESSING

    override fun execute(descriptor: CommandDescriptor): Result<ResponderProcessingResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<ResponderProcessingRequest.Params>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return responderService.responderProcessing(params = params)
    }
}
