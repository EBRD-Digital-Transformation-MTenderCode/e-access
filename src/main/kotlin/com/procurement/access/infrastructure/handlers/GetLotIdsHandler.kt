package com.procurement.access.infrastructure.handlers

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.lot.LotService
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.dto.lot.GetLotIdsRequest
import com.procurement.access.infrastructure.web.dto.ApiSuccessResponse
import com.procurement.access.model.dto.bpe.getId
import com.procurement.access.model.dto.bpe.getParams
import com.procurement.access.model.dto.bpe.getVersion
import com.procurement.access.utils.toObject
import org.springframework.stereotype.Service

@Service
class GetLotIdsHandler(
    private val lotService: LotService
) {

    fun handle(request: JsonNode): ApiSuccessResponse {

        val id = request.getId()
        val version = request.getVersion()
        val params = request.getParams()
        val data = toObject(GetLotIdsRequest::class.java, params).convert()

        val stage = data.ocid.split("-")[4]

        val result = lotService.getLotIds(
            cpId = data.cpid,
            stage = stage,
            states = data.states
        )

        return ApiSuccessResponse(
            id = id,
            result = result,
            version = version
        )
    }
}
