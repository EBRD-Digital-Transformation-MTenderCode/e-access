package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.FindCriteriaRequest
import com.procurement.access.infrastructure.handler.v2.model.response.FindCriteriaResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class FindCriteriaHandler(
    private val criteriaService: CriteriaService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<FindCriteriaResult>(transform, historyRepositoryOld, historyRepositoryNew, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.FIND_CRITERIA

    override fun execute(descriptor: CommandDescriptor): Result<FindCriteriaResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<FindCriteriaRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return criteriaService.findCriteria(params = params)
    }
}
