package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.GetQualificationCriteriaAndMethodRequest
import com.procurement.access.infrastructure.handler.v2.model.response.GetQualificationCriteriaAndMethodResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class GetQualificationCriteriaAndMethodHandler(
    private val criteriaService: CriteriaService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<GetQualificationCriteriaAndMethodResult>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_QUALIFICATION_CRITERIA_AND_METHOD

    override fun execute(descriptor: CommandDescriptor): Result<GetQualificationCriteriaAndMethodResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetQualificationCriteriaAndMethodRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return criteriaService.getQualificationCriteriaAndMethod(params = params)
    }
}
