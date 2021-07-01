package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.GetDataForContractRequest
import com.procurement.access.infrastructure.handler.v2.model.response.GetDataForContractResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class GetDataForContractHandler(
    private val tenderService: TenderService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<GetDataForContractResponse>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_DATA_FOR_CONTRACT

    override fun execute(descriptor: CommandDescriptor): Result<GetDataForContractResponse, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetDataForContractRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.getDataForContract(params = params)
    }
}
