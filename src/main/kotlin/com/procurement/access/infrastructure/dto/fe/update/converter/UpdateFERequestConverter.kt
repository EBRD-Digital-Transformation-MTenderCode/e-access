package com.procurement.access.infrastructure.dto.fe.update.converter

import com.procurement.access.application.service.fe.update.UpdateFEData
import com.procurement.access.infrastructure.dto.fe.update.UpdateFERequest

fun UpdateFERequest.convert() = UpdateFEData(
    tender = this.tender.convert()
)

fun UpdateFERequest.Tender.convert() = UpdateFEData.Tender(
    title = this.title,
    description = this.description,
    procurementMethodRationale = this.procurementMethodRationale,
    procuringEntity = this.procuringEntity?.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun UpdateFERequest.Tender.Document.convert() = UpdateFEData.Tender.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)

fun UpdateFERequest.Tender.ProcuringEntity.convert() = UpdateFEData.Tender.ProcuringEntity(
    id = this.id,
    persons = this.persons
        .map { it.convert() }
)

fun UpdateFERequest.Tender.ProcuringEntity.Person.convert() = UpdateFEData.Tender.ProcuringEntity.Person(
    id = this.id,
    title = this.title,
    name = this.name,
    identifier = this.identifier.convert(),
    businessFunctions = this.businessFunctions
        .map { it.convert() }
)

fun UpdateFERequest.Tender.ProcuringEntity.Person.Identifier.convert() = UpdateFEData.Tender.ProcuringEntity.Person.Identifier(
    id = this.id,
    scheme = this.scheme,
    uri = this.uri
)

fun UpdateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.convert() = UpdateFEData.Tender.ProcuringEntity.Person.BusinessFunction(
    id = this.id,
    type = this.type,
    jobTitle = this.jobTitle,
    period = this.period.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun UpdateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert() = UpdateFEData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
    startDate = this.startDate
)

fun UpdateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert() = UpdateFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)
