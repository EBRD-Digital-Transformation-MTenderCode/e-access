package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.responder.check.structure.CheckPersonsStructure
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.extension.mapOptionalResult
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonsStructureRequest

fun CheckPersonsStructureRequest.Params.convert(): Result<CheckPersonsStructure.Params, DataErrors> {

    val convertedPersons = this.persons
        .mapResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonsStructure.Params.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        persons = convertedPersons,
        locationOfPersones = this.locationOfPersons
    )
}

private fun CheckPersonsStructureRequest.Params.Person.convert(): Result<CheckPersonsStructure.Params.Person, DataErrors> {
    val identifier = this.identifier
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val businessFunctions = this.businessFunctions
        .mapResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonsStructure.Params.Person.tryCreate(
        title = this.title,
        name = this.name,
        identifier = identifier,
        businessFunctions = businessFunctions

    )
}

private fun CheckPersonsStructureRequest.Params.Person.BusinessFunction.convert(): Result<CheckPersonsStructure.Params.Person.BusinessFunction, DataErrors> {
    val period = this.period
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val documents = this.documents
        .mapOptionalResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonsStructure.Params.Person.BusinessFunction.tryCreate(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = period,
        documents = documents
    )
}

private fun CheckPersonsStructureRequest.Params.Person.BusinessFunction.Document.convert(): Result<CheckPersonsStructure.Params.Person.BusinessFunction.Document, DataErrors> =
    CheckPersonsStructure.Params.Person.BusinessFunction.Document.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        documentType = this.documentType
    )

private fun CheckPersonsStructureRequest.Params.Person.BusinessFunction.Period.convert(): Result<CheckPersonsStructure.Params.Person.BusinessFunction.Period, DataErrors> =
    CheckPersonsStructure.Params.Person.BusinessFunction.Period.tryCreate(
        startDate = this.startDate
    )

private fun CheckPersonsStructureRequest.Params.Person.Identifier.convert(): Result<CheckPersonsStructure.Params.Person.Identifier, DataErrors> =
    CheckPersonsStructure.Params.Person.Identifier.tryCreate(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )
