package com.procurement.access.infrastructure.handler.get.criteria

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class GetQualificationCriteriaAndMethodHandler(
    private val criteriaService: CriteriaService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<Command2Type, GetQualificationCriteriaAndMethodResult>(
    historyRepository = historyDao,
    target = ApiSuccessResponse::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetQualificationCriteriaAndMethodResult, Fail> {

        val paramsNode = node.tryGetParams()
            .orForwardFail { error -> return error }

        val params = paramsNode.tryParamsToObject(GetQualificationCriteriaAndMethodRequest::class.java)
            .orForwardFail { error -> return error }
            .convert()
            .orForwardFail { error -> return error }

        return criteriaService.getQualificationCriteriaAndMethod(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.GET_QUALIFICATION_CRITERIA_AND_METHOD
}
