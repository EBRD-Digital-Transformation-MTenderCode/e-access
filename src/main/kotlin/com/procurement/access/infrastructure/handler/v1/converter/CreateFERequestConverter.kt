package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.fe.create.CreateFEData
import com.procurement.access.infrastructure.handler.v1.model.request.CreateFERequest

fun CreateFERequest.convert() = CreateFEData(
    tender = this.tender.convert()
)

fun CreateFERequest.Tender.convert() = CreateFEData.Tender(
    title = this.title,
    description = this.description,
    procurementMethodRationale = this.procurementMethodRationale,
    procurementMethodModalities = this.procurementMethodModalities.orEmpty(),
    secondStage = this.secondStage?.convert(),
    otherCriteria = this.otherCriteria?.convert(),
    procuringEntity = this.procuringEntity?.convert(),
    criteria = this.criteria
        ?.map { it.convert() }
        .orEmpty(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun CreateFERequest.Tender.SecondStage.convert() = CreateFEData.Tender.SecondStage(
    minimumCandidates = this.minimumCandidates,
    maximumCandidates = this.maximumCandidates
)

fun CreateFERequest.Tender.Document.convert() = CreateFEData.Tender.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)

fun CreateFERequest.Tender.Criteria.convert() = CreateFEData.Tender.Criteria(
    id = this.id,
    description = this.description,
    title = this.title,
    relatesTo = this.relatesTo,
    requirementGroups = this.requirementGroups
        .map { it.convert() }
)

fun CreateFERequest.Tender.Criteria.RequirementGroup.convert() = CreateFEData.Tender.Criteria.RequirementGroup(
    id = this.id,
    description = this.description,
    requirements = this.requirements
        .map { it }
)

fun CreateFERequest.Tender.OtherCriteria.convert() = CreateFEData.Tender.OtherCriteria(
    reductionCriteria = this.reductionCriteria,
    qualificationSystemMethods = this.qualificationSystemMethods
)

fun CreateFERequest.Tender.ProcuringEntity.convert() = CreateFEData.Tender.ProcuringEntity(
    id = this.id,
    persons = this.persons
        .map { it.convert() }
)

fun CreateFERequest.Tender.ProcuringEntity.Person.convert() = CreateFEData.Tender.ProcuringEntity.Person(
    id = this.id,
    title = this.title,
    name = this.name,
    identifier = this.identifier.convert(),
    businessFunctions = this.businessFunctions
        .map { it.convert() }
)

fun CreateFERequest.Tender.ProcuringEntity.Person.Identifier.convert() = CreateFEData.Tender.ProcuringEntity.Person.Identifier(
    id = this.id,
    scheme = this.scheme,
    uri = this.uri
)

fun CreateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.convert() = CreateFEData.Tender.ProcuringEntity.Person.BusinessFunction(
    id = this.id,
    type = this.type,
    jobTitle = this.jobTitle,
    period = this.period.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun CreateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert() = CreateFEData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
    startDate = this.startDate
)

fun CreateFERequest.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert() = CreateFEData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)
