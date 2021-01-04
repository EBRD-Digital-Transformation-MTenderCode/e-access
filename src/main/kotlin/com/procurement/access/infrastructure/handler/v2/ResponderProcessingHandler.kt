package com.procurement.access.infrastructure.handler.v2

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.Transform
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.exception.EmptyStringException
import com.procurement.access.infrastructure.api.v2.CommandTypeV2
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.handler.v1.converter.convert
import com.procurement.access.infrastructure.handler.v2.base.AbstractHistoricalHandler
import com.procurement.access.infrastructure.handler.v2.model.request.ResponderProcessingRequest
import com.procurement.access.infrastructure.handler.v2.model.response.ResponderProcessingResult
import com.procurement.access.lib.errorIfBlank
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.service.ResponderService
import org.springframework.stereotype.Service

@Service
class ResponderProcessingHandler(
    private val responderService: ResponderService,
    transform: Transform,
    historyRepository: HistoryRepository,
    logger: Logger
) : AbstractHistoricalHandler<ResponderProcessingResult>(transform, historyRepository, logger) {

    override val action: CommandTypeV2
        get() = CommandTypeV2.RESPONDER_PROCESSING

    override fun execute(descriptor: CommandDescriptor): Result<ResponderProcessingResult, Fail> {
        val params = descriptor.body.asJsonNode
            .params<ResponderProcessingRequest.Params>()
            .flatMap { it.validateTextAttributes() }
            .flatMap { it.convert() }
            .onFailure { return it }
        return responderService.responderProcessing(params = params)
    }

    private fun ResponderProcessingRequest.Params.validateTextAttributes(): Result<ResponderProcessingRequest.Params, DataErrors.Validation.EmptyString> {
        try {
            responder.title.checkForBlank("responder.title")
            responder.name.checkForBlank("responder.name")
            responder.identifier.scheme.checkForBlank("responder.identifier.scheme")
            responder.identifier.id.checkForBlank("responder.identifier.id")
            responder.identifier.uri.checkForBlank("responder.identifier.uri")
            responder.businessFunctions.forEachIndexed { i, businessFunction ->
                businessFunction.id.checkForBlank("responder.businessFunctions[$i].id")
                businessFunction.jobTitle.checkForBlank("responder.businessFunctions[$i].jobTitle")
                businessFunction.documents?.forEachIndexed { j, document ->
                    document.title.checkForBlank("responder.documents[$j].title")
                    document.description.checkForBlank("responder.documents[$j].description")
                }
            }
        } catch (exception: EmptyStringException) {
            return DataErrors.Validation.EmptyString(exception.attributeName).asFailure()
        }

        return this.asSuccess()
    }

    private fun String?.checkForBlank(name: String) = this.errorIfBlank { EmptyStringException(name) }
}
