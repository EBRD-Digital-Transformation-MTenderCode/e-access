package com.procurement.access.infrastructure.handler.find.auction

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractQueryHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class FindAuctionsHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandler<Command2Type, FindAuctionsResult?>(logger = logger) {

    override fun execute(node: JsonNode): Result<FindAuctionsResult?, Fail> {

        val paramsNode = node.tryGetParams()
            .orForwardFail { error -> return error }

        val params = paramsNode.tryParamsToObject(FindAuctionsRequest::class.java)
            .orForwardFail { error -> return error }
            .convert()
            .orForwardFail { error -> return error }

        return tenderService.findAuctions(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.FIND_AUCTIONS
}
