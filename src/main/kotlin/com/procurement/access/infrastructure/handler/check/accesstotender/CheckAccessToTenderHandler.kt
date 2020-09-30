package com.procurement.access.infrastructure.handler.check.accesstotender

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.domain.util.bind
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.validation.ValidationService
import org.springframework.stereotype.Service

@Service
class CheckAccessToTenderHandler(
    private val logger: Logger,
    private val validationService: ValidationService
) : AbstractValidationHandler<Command2Type>(logger = logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {

        val params = node.tryGetParams()
            .bind { it.tryParamsToObject(CheckAccessToTenderRequest::class.java) }
            .bind { it.convert() }
            .doReturn { error -> return ValidationResult.error(error) }

        return validationService.checkOwnerAndToken(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CHECK_ACCESS_TO_TENDER
}
