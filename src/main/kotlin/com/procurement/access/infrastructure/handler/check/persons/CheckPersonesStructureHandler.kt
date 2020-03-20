package com.procurement.access.infrastructure.handler.check.persons

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructureParams
import com.procurement.access.application.service.Logger
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.BadRequestErrors
import com.procurement.access.domain.util.ValidationResult
import com.procurement.access.infrastructure.dto.converter.convert
import com.procurement.access.infrastructure.handler.AbstractValidationHandler
import com.procurement.access.model.dto.bpe.Command2Type
import com.procurement.access.model.dto.bpe.tryGetParams
import com.procurement.access.service.ResponderService
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service

@Service
class CheckPersonesStructureHandler(
    private val responderService: ResponderService,
    private val logger: Logger
) : AbstractValidationHandler<Command2Type>(logger) {

    override fun execute(node: JsonNode): ValidationResult<Fail> {
        val paramsNode = node.tryGetParams()
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        val params: CheckPersonesStructureParams = paramsNode.tryToObject(CheckPersonesStructureRequest::class.java)
            .doOnError { error ->
                error.logging(logger)
                return ValidationResult.error(
                    BadRequestErrors.Parsing(
                        message = "Can not parse to ${error.className}",
                        request = paramsNode.toString()
                    )
                )
            }
            .get
            .convert()
            .doOnError { error -> return ValidationResult.error(error) }
            .get

        return responderService.checkPersonesStructure(params = params)
    }

    override val action: Command2Type
        get() = Command2Type.CHECK_PERSONES_STRUCTURE
}
