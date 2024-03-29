package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractValidationHandlerV2
import com.procurement.access.infrastructure.handler.v2.model.request.ValidateLotsDataForDivisionRequest
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.LotsService
import org.springframework.stereotype.Service

@Service
class ValidateLotsDataForDivisionHandler(
    private val lotsService: LotsService,
    logger: Logger
) : AbstractValidationHandlerV2(logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.VALIDATE_LOTS_DATA_FOR_DIVISION

    override fun execute(descriptor: CommandDescriptor): ValidationResult<Fail> {
        val params = descriptor.body.asJsonNode
            .params<ValidateLotsDataForDivisionRequest>()
            .flatMap { it.convert() }
            .onFailure { return it.reason.asValidationFailure() }
        return lotsService.validateLotsDataForDivision(params = params)
    }
}
