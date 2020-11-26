package com.procurement.access.infrastructure.dto.converter.verify

import com.procurement.access.application.model.responder.verify.VerifyRequirementResponse
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.infrastructure.handler.verify.VerifyRequirementResponseRequest
import com.procurement.access.lib.extension.mapResult

fun VerifyRequirementResponseRequest.Params.convert(): Result<VerifyRequirementResponse.Params, DataErrors> {

    val convertedResponder = this.responder
        .convert()
        .orForwardFail { error -> return error }

    return VerifyRequirementResponse.Params.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        requirementId = this.requirementId,
        requirementResponseId = this.requirementResponseId,
        value = this.value,
        responder = convertedResponder
    )
}

private fun VerifyRequirementResponseRequest.Params.Responder.convert(): Result<VerifyRequirementResponse.Params.Responder, DataErrors> {
    val identifier = this.identifier
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val businessFunctions = this.businessFunctions
        .mapResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return VerifyRequirementResponse.Params.Responder.tryCreate(
        title = this.title,
        name = this.name,
        identifier = identifier,
        businessFunctions = businessFunctions
    )
}

private fun VerifyRequirementResponseRequest.Params.Responder.BusinessFunction.convert(): Result<VerifyRequirementResponse.Params.Responder.BusinessFunction, DataErrors> {
    val period = this.period
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val documents = this.documents
        ?.mapResult { it.convert() }
        ?.doOnError { error -> return failure(error) }
        ?.get

    return VerifyRequirementResponse.Params.Responder.BusinessFunction.tryCreate(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = period,
        documents = documents
    )
}

private fun VerifyRequirementResponseRequest.Params.Responder.BusinessFunction.Document.convert(): Result<VerifyRequirementResponse.Params.Responder.BusinessFunction.Document, DataErrors> =
    VerifyRequirementResponse.Params.Responder.BusinessFunction.Document.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        documentType = this.documentType
    )

private fun VerifyRequirementResponseRequest.Params.Responder.BusinessFunction.Period.convert(): Result<VerifyRequirementResponse.Params.Responder.BusinessFunction.Period, DataErrors> =
    VerifyRequirementResponse.Params.Responder.BusinessFunction.Period.tryCreate(
        startDate = this.startDate
    )

private fun VerifyRequirementResponseRequest.Params.Responder.Identifier.convert(): Result<VerifyRequirementResponse.Params.Responder.Identifier, DataErrors> =
    VerifyRequirementResponse.Params.Responder.Identifier.tryCreate(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )
