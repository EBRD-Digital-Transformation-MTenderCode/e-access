package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.responder.processing.ResponderProcessingParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.extension.mapOptionalResult
import com.procurement.access.infrastructure.handler.processing.responder.ResponderProcessingRequest

fun ResponderProcessingRequest.convert(): Result<ResponderProcessingParams, DataErrors> {

    val responder = this.responder
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    return ResponderProcessingParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        startDate = this.startDate,
        responder = responder
    )
}

private fun ResponderProcessingRequest.Responder.convert(): Result<ResponderProcessingParams.Responder, DataErrors> {
    val identifier = this.identifier
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val businessFunctions = this.businessFunctions
        .mapOptionalResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return ResponderProcessingParams.Responder.tryCreate(
        title = this.title,
        name = this.name,
        identifier = identifier,
        businessFunctions = businessFunctions

    )
}

private fun ResponderProcessingRequest.Responder.BusinessFunction.convert(): Result<ResponderProcessingParams.Responder.BusinessFunction, DataErrors> {
    val period = this.period
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val documents = this.documents
        .mapOptionalResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return ResponderProcessingParams.Responder.BusinessFunction.tryCreate(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = period,
        documents = documents
    )
}

private fun ResponderProcessingRequest.Responder.BusinessFunction.Document.convert(): Result<ResponderProcessingParams.Responder.BusinessFunction.Document, DataErrors> =
    ResponderProcessingParams.Responder.BusinessFunction.Document.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        documentType = this.documentType
    )

private fun ResponderProcessingRequest.Responder.BusinessFunction.Period.convert(): Result<ResponderProcessingParams.Responder.BusinessFunction.Period, DataErrors> =
    ResponderProcessingParams.Responder.BusinessFunction.Period.tryCreate(
        startDate = this.startDate
    )

private fun ResponderProcessingRequest.Responder.Identifier.convert(): Result<ResponderProcessingParams.Responder.Identifier, DataErrors> =
    ResponderProcessingParams.Responder.Identifier.tryCreate(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )
