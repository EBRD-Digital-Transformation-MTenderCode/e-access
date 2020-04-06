package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructure
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.extension.mapOptionalResult
import com.procurement.access.domain.util.extension.mapResult
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonesStructureRequest

fun CheckPersonesStructureRequest.Params.convert(): Result<CheckPersonesStructure.Params, DataErrors> {

    val convertedPersons = this.persons
        .mapResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonesStructure.Params.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        persons = convertedPersons,
        locationOfPersones = this.locationOfPersons
    )
}

private fun CheckPersonesStructureRequest.Params.Person.convert(): Result<CheckPersonesStructure.Params.Person, DataErrors> {
    val identifier = this.identifier
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val businessFunctions = this.businessFunctions
        .mapResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonesStructure.Params.Person.tryCreate(
        title = this.title,
        name = this.name,
        identifier = identifier,
        businessFunctions = businessFunctions

    )
}

private fun CheckPersonesStructureRequest.Params.Person.BusinessFunction.convert(): Result<CheckPersonesStructure.Params.Person.BusinessFunction, DataErrors> {
    val period = this.period
        .convert()
        .doOnError { error -> return failure(error) }
        .get

    val documents = this.documents
        .mapOptionalResult { it.convert() }
        .doOnError { error -> return failure(error) }
        .get

    return CheckPersonesStructure.Params.Person.BusinessFunction.tryCreate(
        id = this.id,
        jobTitle = this.jobTitle,
        type = this.type,
        period = period,
        documents = documents
    )
}

private fun CheckPersonesStructureRequest.Params.Person.BusinessFunction.Document.convert(): Result<CheckPersonesStructure.Params.Person.BusinessFunction.Document, DataErrors> =
    CheckPersonesStructure.Params.Person.BusinessFunction.Document.tryCreate(
        id = this.id,
        title = this.title,
        description = this.description,
        documentType = this.documentType
    )

private fun CheckPersonesStructureRequest.Params.Person.BusinessFunction.Period.convert(): Result<CheckPersonesStructure.Params.Person.BusinessFunction.Period, DataErrors> =
    CheckPersonesStructure.Params.Person.BusinessFunction.Period.tryCreate(
        startDate = this.startDate
    )

private fun CheckPersonesStructureRequest.Params.Person.Identifier.convert(): Result<CheckPersonesStructure.Params.Person.Identifier, DataErrors> =
    CheckPersonesStructure.Params.Person.Identifier.tryCreate(
        id = this.id,
        scheme = this.scheme,
        uri = this.uri
    )
