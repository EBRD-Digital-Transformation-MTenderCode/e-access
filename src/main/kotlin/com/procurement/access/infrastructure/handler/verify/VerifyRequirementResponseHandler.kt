package com.procurement.access.infrastructure.handler.verify

import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.dto.converter.verify.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class VerifyRequirementResponseHandler(
    private val responderService: ResponderService,
    logger: Logger
) : AbstractValidationHandler(logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.VERIFY_REQUIREMENT_RESPONSE

    override fun execute(descriptor: CommandDescriptor): ValidationResult<Fail> {
        val params = descriptor.body.asJsonNode
            .params<VerifyRequirementResponseRequest.Params>()
            .flatMap { it.convert() }
            .onFailure { return it.reason.asValidationFailure() }
        return responderService.verifyRequirementResponse(params = params)
    }
}
