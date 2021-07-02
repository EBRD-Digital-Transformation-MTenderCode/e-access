package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRelationToOtherProcessRequest
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRelationToOtherProcessResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.OutsourcingService
import org.springframework.stereotype.Service

@Service
class CreateRelationToOtherProcessHandler(
    transform: Transform,
    private val outsourcingService: OutsourcingService,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<CreateRelationToOtherProcessResult>(
    transform,
    historyRepositoryOld,
    historyRepositoryNew,
    logger
) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.CREATE_RELATION_TO_OTHER_PROCESS

    override fun execute(descriptor: CommandDescriptor): Result<CreateRelationToOtherProcessResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<CreateRelationToOtherProcessRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return outsourcingService.createRelationToOtherProcess(params = params)
    }
}
