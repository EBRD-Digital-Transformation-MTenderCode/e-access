package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.CreateCriteriaForProcuringEntityRequest
import com.procurement.access.infrastructure.handler.v2.model.response.CreateCriteriaForProcuringEntityResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class CreateCriteriaForProcuringEntityHandler(
    private val criteriaService: CriteriaService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<CreateCriteriaForProcuringEntityResult>(
    transform,
    historyRepositoryOld,
    historyRepositoryNew,
    logger
) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.CREATE_CRITERIA_FOR_PROCURING_ENTITY

    override fun execute(descriptor: CommandDescriptor): Result<CreateCriteriaForProcuringEntityResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<CreateCriteriaForProcuringEntityRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return criteriaService.createCriteriaForProcuringEntity(params = params)
    }
}
