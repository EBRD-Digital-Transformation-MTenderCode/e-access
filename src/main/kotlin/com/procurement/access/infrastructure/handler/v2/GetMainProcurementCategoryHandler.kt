package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.get.procurement.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.GetMainProcurementCategoryRequest
import com.procurement.access.infrastructure.handler.v2.model.response.GetMainProcurementCategoryResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class GetMainProcurementCategoryHandler(
    private val tenderService: TenderService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<GetMainProcurementCategoryResult>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_MAIN_PROCUREMENT_CATEGORY

    override fun execute(descriptor: CommandDescriptor): Result<GetMainProcurementCategoryResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetMainProcurementCategoryRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.getMainProcurementCategory(params = params)
    }
}
