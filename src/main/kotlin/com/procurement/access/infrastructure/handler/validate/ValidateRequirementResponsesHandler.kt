package com.procurement.access.infrastructure.handler.validate

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.validate.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class ValidateRequirementResponsesHandler(
    private val responderService: ResponderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<CommandTypeV2, ValidateRequirementResponsesResult>(
    historyRepository = historyDao,
    target = ApiResponseV2.Success::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<ValidateRequirementResponsesResult, Fail> {
        val params = node.tryGetParams()
            .onFailure { error -> return error }
            .tryParamsToObject(ValidateRequirementResponsesRequest::class.java)
            .onFailure { error -> return error }
            .convert()
            .onFailure { error -> return error }

        return responderService.validateRequirementResponses(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.VALIDATE_REQUIREMENT_RESPONSES
}
