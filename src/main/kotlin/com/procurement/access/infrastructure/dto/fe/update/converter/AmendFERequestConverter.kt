package com.procurement.access.infrastructure.dto.fe.update.converter

import com.procurement.access.application.service.fe.update.AmendFEData
import com.procurement.access.infrastructure.dto.fe.update.AmendFERequest

fun AmendFERequest.convert() = AmendFEData(
    tender = this.tender.convert()
)

fun AmendFERequest.Tender.convert() = AmendFEData.Tender(
    title = this.title,
    description = this.description,
    procurementMethodRationale = this.procurementMethodRationale,
    procuringEntity = this.procuringEntity?.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun AmendFERequest.Tender.Document.convert() = AmendFEData.Tender.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)

fun AmendFERequest.Tender.ProcuringEntity.convert() = AmendFEData.Tender.ProcuringEntity(
    id = this.id,
    persons = this.persons
        .map { it.convert() }
)

fun AmendFERequest.Tender.ProcuringEntity.Person.convert() = AmendFEData.Tender.ProcuringEntity.Person(
    id = this.id,
    title = this.title,
    name = this.name,
    identifier = this.identifier.convert(),
    businessFunctions = this.businessFunctions
        .map { it.convert() }
)

fun AmendFERequest.Tender.ProcuringEntity.Person.Identifier.convert() = AmendFEData.Tender.ProcuringEntity.Person.Identifier(
    id = this.id,
    scheme = this.scheme,
    uri = this.uri
)

fun AmendFERequest.Tender.ProcuringEntity.Person.BusinessFunction.convert() = AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction(
    id = this.id,
    type = this.type,
    jobTitle = this.jobTitle,
    period = this.period.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun AmendFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert() = AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
    startDate = this.startDate
)

fun AmendFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert() = AmendFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)
