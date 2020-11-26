package com.procurement.access.infrastructure.handler.find.auction

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractQueryHandler
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.dto.bpe.CommandTypeV2
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class FindAuctionsHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandler<CommandTypeV2, FindAuctionsResult?>(logger = logger) {

    override fun execute(node: JsonNode): Result<FindAuctionsResult?, Fail> {

        val paramsNode = node.tryGetParams()
            .onFailure { error -> return error }

        val params = paramsNode.tryParamsToObject(FindAuctionsRequest::class.java)
            .onFailure { error -> return error }
            .convert()
            .onFailure { error -> return error }

        return tenderService.findAuctions(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.FIND_AUCTIONS
}
