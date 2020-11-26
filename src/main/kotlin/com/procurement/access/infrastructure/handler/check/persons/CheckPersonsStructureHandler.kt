package com.procurement.access.infrastructure.handler.check.persons

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.lib.functional.ValidationResult
import com.procurement.access.lib.functional.asValidationFailure
import com.procurement.access.model.dto.bpe.CommandTypeV2
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.model.dto.bpe.tryParamsToObject
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class CheckPersonsStructureHandler(
    private val responderService: ResponderService,
    logger: Logger
) : AbstractValidationHandler<CommandTypeV2>(logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {
        val params = node.tryGetParams()
            .onFailure { return it.reason.asValidationFailure() }
            .tryParamsToObject(CheckPersonesStructureRequest.Params::class.java)
            .onFailure { return it.reason.asValidationFailure() }
            .convert()
            .onFailure { return it.reason.asValidationFailure() }

        return responderService.checkPersonesStructure(params = params)
    }

    override val action: CommandTypeV2
        get() = CommandTypeV2.CHECK_PERSONES_STRUCTURE
}
