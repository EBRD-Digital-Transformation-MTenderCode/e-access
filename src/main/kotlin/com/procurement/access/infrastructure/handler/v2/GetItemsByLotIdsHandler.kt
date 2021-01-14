package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.tender.strategy.get.items.GetItemsByLotIdsResult
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractQueryHandlerV2
import com.procurement.access.infrastructure.handler.v2.model.request.GetItemsByLotIdsRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.TenderService
import org.springframework.stereotype.Component

@Component
class GetItemsByLotIdsHandler(
    private val tenderService: TenderService,
    logger: Logger
) : AbstractQueryHandlerV2<GetItemsByLotIdsResult>(logger = logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_ITEMS_BY_LOT_IDS

    override fun execute(descriptor: CommandDescriptor): Result<GetItemsByLotIdsResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetItemsByLotIdsRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }

        return tenderService.getItemsByLotIds(params = params)
    }
}