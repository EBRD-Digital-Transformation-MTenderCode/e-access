package com.procurement.access.infrastructure.handler.validate

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.validate.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class ValidateRequirementResponsesHandler(
    private val responderService: ResponderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, ValidateRequirementResponsesResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<ValidateRequirementResponsesResult, Fail> {
        val params = node.tryGetParams()
            .orForwardFail { error -> return error }
            .tryParamsToObject(ValidateRequirementResponsesRequest::class.java)
            .orForwardFail { error -> return error }
            .convert()
            .orForwardFail { error -> return error }

        return responderService.validateRequirementResponses(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.VALIDATE_REQUIREMENT_RESPONSES
}
