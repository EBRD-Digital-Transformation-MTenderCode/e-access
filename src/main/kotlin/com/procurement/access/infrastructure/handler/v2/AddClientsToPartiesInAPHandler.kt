package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.AddClientsToPartiesInAPRequest
import com.procurement.access.infrastructure.handler.v2.model.response.AddClientsToPartiesInAPResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.APService
import org.springframework.stereotype.Service

@Service
class AddClientsToPartiesInAPHandler(
    private val apService: APService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<AddClientsToPartiesInAPResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.ADD_CLIENTS_TO_PARTIES_IN_AP

    override fun execute(descriptor: CommandDescriptor): Result<AddClientsToPartiesInAPResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<AddClientsToPartiesInAPRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }

        return apService.addClientsToPartiesInAP(params = params)
    }
}
