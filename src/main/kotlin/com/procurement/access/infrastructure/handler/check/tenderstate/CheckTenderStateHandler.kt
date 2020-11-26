package com.procurement.access.infrastructure.handler.check.tenderstate

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.model.dto.bpe.CommandTypeV2
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.validation.ValidationService
import org.springframework.stereotype.Service

@Service
class CheckTenderStateHandler(
    private val logger: Logger,
    private val validationService: ValidationService
) : AbstractValidationHandler<CommandTypeV2>(logger = logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {

        val params = node.tryGetParams()
            .onFailure { return ValidationResult.error(it.reason) }
            .tryParamsToObject(CheckTenderStateRequest::class.java)
            .onFailure { return ValidationResult.error(it.reason) }
            .convert()
            .onFailure { return ValidationResult.error(it.reason) }

        return validationService.checkTenderState(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.CHECK_TENDER_STATE
}
