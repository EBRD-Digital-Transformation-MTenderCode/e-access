package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.PersonesProcessingParams
import com.procurement.access.application.model.parseBusinessFunctionDocumentType
import com.procurement.access.application.model.parseBusinessFunctionType
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseDate
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parsePersonId
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.infrastructure.handler.v2.model.request.PersonesProcessingRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.validate

fun PersonesProcessingRequest.convert(): Result<PersonesProcessingParams, DataErrors> {
    val parties = parties.validate(notEmptyRule("parties"))
        .onFailure { return it }

    return PersonesProcessingParams(
        cpid = parseCpid(cpid).onFailure { return it },
        ocid = parseOcid(ocid).onFailure { return it },
        role = role,
        parties = parties.mapResult { it.convert() }.onFailure { return it }

    ).asSuccess()
}

private fun PersonesProcessingRequest.Party.convert(): Result<PersonesProcessingParams.Party, DataErrors> {
    val persones = persones.validate(notEmptyRule("parties.persones"))
        .onFailure { return it }
        .mapResult { it.convert() }
        .onFailure { return it }

    return PersonesProcessingParams.Party(
        id = id,
        persones = persones
    ).asSuccess()
}

private fun PersonesProcessingRequest.Party.Persone.convert(): Result<PersonesProcessingParams.Party.Persone, DataErrors> {
    val businessFunctions = businessFunctions.validate(notEmptyRule("parties.persones.businessFunctions"))
        .onFailure { return it }
        .mapResult { it.convert() }
        .onFailure { return it }

    return PersonesProcessingParams.Party.Persone(
        id = parsePersonId(id, "parties.persones.id").onFailure { return it },
        title = title,
        name = name,
        identifier = identifier.let { identifier ->
            PersonesProcessingParams.Party.Persone.Identifier(
                id = identifier.id,
                scheme = identifier.scheme,
                uri = identifier.uri
            )
        },
        businessFunctions = businessFunctions
    ).asSuccess()
}

val allowedBusinessFunctionType = BusinessFunctionType.allowedElements
    .filter {
        when (it) {
            BusinessFunctionType.AUTHORITY,
            BusinessFunctionType.CHAIRMAN,
            BusinessFunctionType.CONTACT_POINT,
            BusinessFunctionType.PRICE_EVALUATOR,
            BusinessFunctionType.PRICE_OPENER,
            BusinessFunctionType.PROCURMENT_OFFICER,
            BusinessFunctionType.TECHNICAL_EVALUATOR,
            BusinessFunctionType.TECHNICAL_OPENER -> true
        }
    }
    .toSet()

private fun PersonesProcessingRequest.Party.Persone.BusinessFunction.convert(): Result<PersonesProcessingParams.Party.Persone.BusinessFunction, DataErrors> {
    val documents = documents.validate(notEmptyRule("parties.persones.businessFunctions.documents"))
        .onFailure { return it }
        ?.mapResult { it.convert() }
        ?.onFailure { return it }

    return PersonesProcessingParams.Party.Persone.BusinessFunction(
        id = id,
        jobTitle = jobTitle,
        type = parseBusinessFunctionType(type, allowedBusinessFunctionType, "parties.persones.businessFunctions.type")
            .onFailure { return it },
        period = PersonesProcessingParams.Party.Persone.BusinessFunction.Period(
            startDate = parseDate(period.startDate, "parties.persones.businessFunctions.period.startDate")
                .onFailure { return it }
        ),
        documents = documents
    ).asSuccess()
}

val allowedDocumentTypes = BusinessFunctionDocumentType.allowedElements
    .filter {
        when (it) {
            BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> true
        }
    }
    .toSet()

private fun PersonesProcessingRequest.Party.Persone.BusinessFunction.Document.convert(): Result<PersonesProcessingParams.Party.Persone.BusinessFunction.Document, DataErrors> =
    PersonesProcessingParams.Party.Persone.BusinessFunction.Document(
        id = id,
        title = title,
        description = description,
        documentType = parseBusinessFunctionDocumentType(
            documentType,
            allowedDocumentTypes,
            "parties.persones.businessFunctions.documents.documentType"
        )
            .onFailure { return it }
    ).asSuccess()

