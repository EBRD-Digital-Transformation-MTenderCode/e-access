package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.PersonesProcessingRequest
import com.procurement.access.infrastructure.handler.v2.model.response.PersonesProcessingResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class PersonesProcessingHandler(
    private val responderService: ResponderService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<PersonesProcessingResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.PERSONES_PROCESSING

    override fun execute(descriptor: CommandDescriptor): Result<PersonesProcessingResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<PersonesProcessingRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return responderService.personesProcessing(params = params)
    }
}
