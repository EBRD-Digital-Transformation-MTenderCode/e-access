package com.procurement.access.infrastructure.handler.get.organization

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.dao.HistoryDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.get.organization.convert
import com.procurement.access.infrastructure.handler.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class GetOrganizationHandler(
    private val responderService: ResponderService,
    transform: Transform,
    historyDao: HistoryDao,
    logger: Logger
) : AbstractHistoricalHandler<GetOrganizationResult>(transform, historyDao, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.GET_ORGANIZATION

    override fun execute(descriptor: CommandDescriptor): Result<GetOrganizationResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<GetOrganizationRequest>()
            .flatMap { it.convert() }
            .onFailure { return it }
        return responderService.getOrganization(params = params)
    }
}
