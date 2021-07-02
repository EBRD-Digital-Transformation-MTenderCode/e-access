package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepositoryNew
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.DefineTenderClassificationRequest
import com.procurement.access.infrastructure.handler.v2.model.response.DefineTenderClassificationResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class DefineTenderClassificationHandler(
    private val tenderService: TenderService,
    transform: Transform,
    historyRepositoryOld: HistoryRepositoryOld,
    historyRepositoryNew: HistoryRepositoryNew,
    logger: Logger
) : AbstractHistoricalHandler<DefineTenderClassificationResult>(
    transform,
    historyRepositoryOld,
    historyRepositoryNew,
    logger
) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.DEFINE_TENDER_CLASSIFICATION

    override fun execute(descriptor: CommandDescriptor): Result<DefineTenderClassificationResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<DefineTenderClassificationRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.defineTenderClassification(params = params)
    }
}
