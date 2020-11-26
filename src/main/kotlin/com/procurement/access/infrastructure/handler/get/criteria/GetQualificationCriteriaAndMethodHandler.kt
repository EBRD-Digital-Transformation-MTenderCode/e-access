package com.procurement.access.infrastructure.handler.get.criteria

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.dto.converter.get.criteria.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.CommandTypeV2
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.CriteriaService
import org.springframework.stereotype.Service

@Service
class GetQualificationCriteriaAndMethodHandler(
    private val criteriaService: CriteriaService,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<CommandTypeV2, GetQualificationCriteriaAndMethodResult>(
    historyRepository = historyDao,
    target = ApiResponseV2.Success::class.java,
    logger = logger
) {

    override fun execute(node: JsonNode): Result<GetQualificationCriteriaAndMethodResult, Fail> {

        val paramsNode = node.tryGetParams()
            .onFailure { error -> return error }

        val params = paramsNode.tryParamsToObject(GetQualificationCriteriaAndMethodRequest::class.java)
            .onFailure { error -> return error }
            .convert()
            .onFailure { error -> return error }

        return criteriaService.getQualificationCriteriaAndMethod(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_QUALIFICATION_CRITERIA_AND_METHOD
}
