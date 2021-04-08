package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v2.base.AbstractValidationHandlerV2
import com.procurement.access.infrastructure.handler.v2.converter.convert
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateRfqDataRequest
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.RfqService
import org.springframework.stereotype.Service

@Service
class ValidateRfqDataHandler(
    private val rfqService: RfqService,
    logger: Logger
) : AbstractValidationHandlerV2(logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.VALIDATE_RFQ_DATA

    override fun execute(descriptor: CommandDescriptor): ValidationResult<Fail> {
        val params = descriptor.body.asJsonNode
            .params<ValidateRfqDataRequest>()
            .flatMap { it.convert() }
            .onFailure { return it.reason.asValidationFailure() }
        return rfqService.validateRfqData(params = params)
    }
}