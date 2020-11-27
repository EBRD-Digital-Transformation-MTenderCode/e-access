package com.procurement.access.infrastructure.handler.create

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.create.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class CreateCriteriaForProcuringEntityHandler(
    private val criteriaService: CriteriaService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<CreateCriteriaForProcuringEntityResult>(transform, historyDao, logger) {

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
