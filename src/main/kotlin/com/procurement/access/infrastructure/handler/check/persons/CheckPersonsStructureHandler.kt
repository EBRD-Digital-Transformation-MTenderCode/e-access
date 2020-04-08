package com.procurement.access.infrastructure.handler.check.persons

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class CheckPersonsStructureHandler(
    private val responderService: ResponderService,
    logger: Logger
) : AbstractValidationHandler<Command2Type>(logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {
        val params = node.tryGetParams()
            .doOnError { error -> return ValidationResult.error(error) }
            .get
            .tryParamsToObject(CheckPersonesStructureRequest.Params::class.java)
            .doOnError { error -> return ValidationResult.error(error) }
            .get
            .convert()
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        return responderService.checkPersonesStructure(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CHECK_PERSONS_STRUCTURE
}
