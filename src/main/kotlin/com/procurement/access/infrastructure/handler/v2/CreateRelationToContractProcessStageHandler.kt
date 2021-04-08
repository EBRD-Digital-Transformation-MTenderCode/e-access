package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRelationToContractProcessStageRequest
import com.procurement.access.infrastructure.handler.v2.model.response.CreateRelationToContractProcessStageResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.RfqService
import org.springframework.stereotype.Service

@Service
class CreateRelationToContractProcessStageHandler(
    transform: Transform,
    private val rfqService: RfqService,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<CreateRelationToContractProcessStageResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.CREATE_RELATION_TO_CONTRACT_PROCESS_STAGE

    override fun execute(descriptor: CommandDescriptor): Result<CreateRelationToContractProcessStageResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<CreateRelationToContractProcessStageRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return rfqService.createRelationToContractProcessStage(params = params)
    }
}
