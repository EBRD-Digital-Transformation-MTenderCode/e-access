package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.responder.check.structure.CheckPersonesStructure
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.check.persons.CheckPersonesStructureRequest
import com.procurement.access.lib.extension.mapOptionalResult
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result

fun CheckPersonesStructureRequest.Params.convert(): Result<CheckPersonesStructure.Params, DataErrors> {

    val convertedPersones = this.persones
        .mapResult { it.convert() }
        .onFailure { return it }

    return CheckPersonesStructure.Params.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        persones = convertedPersones,
        locationOfPersones = this.locationOfPersones
    )
}

private fun CheckPersonesStructureRequest.Params.Person.convert(): Result<CheckPersonesStructure.Params.Person, DataErrors> {
    val identifier = this.identifier
        .convert()
        .onFailure { return it }

    val businessFunctions = this.businessFunctions
        .mapResult { it.convert() }
        .onFailure { return it }

    return CheckPersonesStructure.Params.Person.tryCreate(
        id = this.id,
        title = this.title,
        name = this.name,
        identifier = identifier,
        businessFunctions = businessFunctions
    )
}

private fun CheckPersonesStructureRequest.Params.Person.BusinessFunction.convert(): Result<CheckPersonesStructure.Params.Person.BusinessFunction, DataErrors> {
    val period = this.period
        .convert()
        .onFailure { return it }

    val documents = this.documents
        .mapOptionalResult { it.convert() }
        .onFailure { return it }

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
