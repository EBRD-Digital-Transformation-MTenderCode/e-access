package com.procurement.access.infrastructure.handler.get.organization

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.get.organization.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.bind
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class GetOrganizationHandler(
    private val responderService: ResponderService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, GetOrganizationResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetOrganizationResult, Fail> {

        val params = node.tryGetParams()
            .bind { it.tryParamsToObject(GetOrganizationRequest::class.java) }
            .bind { it.convert() }
            .orForwardFail { error -> return error }

        return responderService.getOrganization(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.GET_ORGANIZATION
}
