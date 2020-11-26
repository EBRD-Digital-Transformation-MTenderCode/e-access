package com.procurement.access.infrastructure.handler.check.currency

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.validation.ValidationService
import org.springframework.stereotype.Service

@Service
class CheckEqualityCurrenciesHandler(
    private val logger: Logger,
    private val validationService: ValidationService
) : AbstractValidationHandler<Command2Type>(logger = logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {

        val params = node.tryGetParams()
            .flatMap { it.tryParamsToObject(CheckEqualityCurrenciesRequest::class.java) }
            .flatMap { it.convert() }
            .onFailure { return ValidationResult.error(it.reason) }

        return validationService.checkEqualityCurrencies(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CHECK_EQUALITY_CURRENCIES
}
