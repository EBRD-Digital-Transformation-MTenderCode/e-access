package com.procurement.access.infrastructure.dto.fe.check.converter

import com.procurement.access.application.service.fe.check.CheckFEDataData
import com.procurement.access.domain.EnumElementProviderParser
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.dto.fe.check.CheckFEDataRequest
import com.procurement.access.lib.errorIfEmpty
import com.procurement.access.lib.mapIfNotEmpty
import com.procurement.access.lib.orThrow

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
        .errorIfEmpty {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The tender contain empty list of the criteria."
            )
        }
        ?.map { it.convert() }
        .orEmpty(),
    documents = this.documents
        .errorIfEmpty {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The tender contain empty list of the documents."
            )
        }
        ?.map { it.convert() }
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
    documentType = checkAndGetDocumentType(this.documentType)
)

private fun checkAndGetDocumentType(documentType: String): DocumentType {
    return EnumElementProviderParser.checkAndParseEnum(
        value = documentType,
        allowedValues = allowedTenderDocumentTypes,
        target = DocumentType
    )
}

private val allowedTenderDocumentTypes = DocumentType.allowedElements
    .filter {
        when (it) {
            DocumentType.TENDER_NOTICE,
            DocumentType.BIDDING_DOCUMENTS,
            DocumentType.TECHNICAL_SPECIFICATIONS,
            DocumentType.EVALUATION_CRITERIA,
            DocumentType.CLARIFICATIONS,
            DocumentType.ELIGIBILITY_CRITERIA,
            DocumentType.RISK_PROVISIONS,
            DocumentType.BILL_OF_QUANTITY,
            DocumentType.CONFLICT_OF_INTEREST,
            DocumentType.PROCUREMENT_PLAN,
            DocumentType.CONTRACT_DRAFT,
            DocumentType.COMPLAINTS,
            DocumentType.ILLUSTRATION,
            DocumentType.CANCELLATION_DETAILS,
            DocumentType.EVALUATION_REPORTS,
            DocumentType.SHORTLISTED_FIRMS,
            DocumentType.CONTRACT_ARRANGEMENTS,
            DocumentType.CONTRACT_GUARANTEES -> true

            DocumentType.ASSET_AND_LIABILITY_ASSESSMENT,
            DocumentType.ENVIRONMENTAL_IMPACT,
            DocumentType.FEASIBILITY_STUDY,
            DocumentType.HEARING_NOTICE,
            DocumentType.MARKET_STUDIES,
            DocumentType.NEEDS_ASSESSMENT,
            DocumentType.PROJECT_PLAN -> false
        }
    }.toSet()

fun CheckFEDataRequest.Tender.Criteria.convert() = CheckFEDataData.Tender.Criteria(
    id = this.id,
    description = this.description,
    title = this.title,
    relatesTo = this.relatesTo,
    requirementGroups = this.requirementGroups
        .mapIfNotEmpty { it.convert() }
        .orThrow {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The tender.criteria contain empty list of the requirement groups."
            )
        }
)

fun CheckFEDataRequest.Tender.Criteria.RequirementGroup.convert() = CheckFEDataData.Tender.Criteria.RequirementGroup(
    id = this.id,
    description = this.description,
    requirements = this.requirements
        .mapIfNotEmpty { it }
        .orThrow {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The criteria.requirements contain empty list of the requirements."
            )
        }
)

fun CheckFEDataRequest.Tender.OtherCriteria.convert() = CheckFEDataData.Tender.OtherCriteria(
    reductionCriteria = this.reductionCriteria,
    qualificationSystemMethods = this.qualificationSystemMethods
)

fun CheckFEDataRequest.Tender.ProcuringEntity.convert() = CheckFEDataData.Tender.ProcuringEntity(
    id = this.id,
    persons = this.persons
        .mapIfNotEmpty { it.convert() }
        .orThrow {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The procuringentity contain empty list of the persones."
            )
        }
)

fun CheckFEDataRequest.Tender.ProcuringEntity.Person.convert() = CheckFEDataData.Tender.ProcuringEntity.Person(
    id = this.id,
    title = this.title,
    name = this.name,
    identifier = this.identifier.convert(),
    businessFunctions = this.businessFunctions
        .mapIfNotEmpty { it.convert() }
        .orThrow {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The procuringEntity.persons contain empty list of the businessFunctions."
            )
        }
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
        .errorIfEmpty {
            ErrorException(
                error = ErrorType.IS_EMPTY,
                message = "The person business functions contain empty list of the documents."
            )
        }
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




