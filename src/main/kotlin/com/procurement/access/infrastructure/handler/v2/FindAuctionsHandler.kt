package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractQueryHandlerV2
import com.procurement.access.infrastructure.handler.v2.model.request.FindAuctionsRequest
import com.procurement.access.infrastructure.handler.v2.model.response.FindAuctionsResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Service

@Service
class FindAuctionsHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandlerV2<FindAuctionsResult?>(logger = logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.FIND_AUCTIONS

    override fun execute(descriptor: CommandDescriptor): Result<FindAuctionsResult?, Fail> {
        val params = descriptor.body.asJsonNode
            .params<FindAuctionsRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return tenderService.findAuctions(params = params)
    }
}
