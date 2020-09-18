package com.procurement.access.infrastructure.dto.fe.check.converter

import com.procurement.access.application.service.fe.check.CheckFEDataData
import com.procurement.access.infrastructure.dto.fe.check.CheckFEDataRequest

fun CheckFEDataRequest.convert() = CheckFEDataData(
    tender = this.tender.convert()
)

fun CheckFEDataRequest.Tender.convert() = CheckFEDataData.Tender(
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
    documents = this.documents?.map { it.convert() }
        .orEmpty()
)

fun CheckFEDataRequest.Tender.SecondStage.convert() = CheckFEDataData.Tender.SecondStage(
    minimumCandidates = this.minimumCandidates,
    maximumCandidates = this.maximumCandidates
)

fun CheckFEDataRequest.Tender.Document.convert() = CheckFEDataData.Tender.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)

fun CheckFEDataRequest.Tender.Criteria.convert() = CheckFEDataData.Tender.Criteria(
    id = this.id,
    description = this.description,
    title = this.title,
    relatesTo = this.relatesTo,
    requirementGroups = this.requirementGroups.map { it.convert() }
)

fun CheckFEDataRequest.Tender.Criteria.RequirementGroup.convert() = CheckFEDataData.Tender.Criteria.RequirementGroup(
    id = this.id,
    description = this.description,
    requirements = this.requirements
)

fun CheckFEDataRequest.Tender.OtherCriteria.convert() = CheckFEDataData.Tender.OtherCriteria(
    reductionCriteria = this.reductionCriteria,
    qualificationSystemMethods = this.qualificationSystemMethods
)

fun CheckFEDataRequest.Tender.ProcuringEntity.convert() = CheckFEDataData.Tender.ProcuringEntity(
    id = this.id,
    persons = this.persons.map { it.convert() }
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.convert() = CheckFEDataData.Tender.ProcuringEntity.Person(
    id = this.id,
    title = this.title,
    name = this.name,
    identifier = this.identifier.convert(),
    businessFunctions = this.businessFunctions.map { it.convert() }
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.Identifier.convert() = CheckFEDataData.Tender.ProcuringEntity.Person.Identifier(
    id = this.id,
    scheme = this.scheme,
    uri = this.uri
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.BusinessFunction.convert() = CheckFEDataData.Tender.ProcuringEntity.Person.BusinessFunction(
    id = this.id,
    type = this.type,
    jobTitle = this.jobTitle,
    period = this.period.convert(),
    documents = this.documents
        ?.map { it.convert() }
        .orEmpty()
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.BusinessFunction.Period.convert() = CheckFEDataData.Tender.ProcuringEntity.Person.BusinessFunction.Period(
    startDate = this.startDate
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.BusinessFunction.Document.convert() = CheckFEDataData.Tender.ProcuringEntity.Person.BusinessFunction.Document(
    id = this.id,
    title = this.title,
    description = this.description,
    documentType = this.documentType
)




