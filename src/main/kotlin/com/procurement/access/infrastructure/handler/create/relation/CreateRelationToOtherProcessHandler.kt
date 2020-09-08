package com.procurement.access.infrastructure.handler.create.relation

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.OutsourcingService
import org.springframework.stereotype.Service

@Service
class CreateRelationToOtherProcessHandler(
    private val outsourcingService: OutsourcingService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, CreateRelationToOtherProcessResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<CreateRelationToOtherProcessResult, Fail> {
        val params = node.tryGetParams()
            .bind { it.tryParamsToObject(CreateRelationToOtherProcessRequest::class.java) }
            .bind { it.convert() }
            .orForwardFail { error -> return error }

        return outsourcingService.createRelationToOtherProcess(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CREATE_RELATION_TO_OTHER_PROCESS
}
