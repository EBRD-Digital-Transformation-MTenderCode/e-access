package com.procurement.access.domain.fail.error

import com.procurement.access.application.service.Logger
import com.procurement.access.application.service.requirement.ValidateRequirementResponsesParams
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.CriteriaSource
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.owner.Owner
import com.procurement.access.domain.model.requirement.RequirementId
import com.procurement.access.domain.model.requirement.response.RequirementResponseId
import com.procurement.access.domain.model.token.Token
import com.procurement.access.infrastructure.dto.cn.criteria.Requirement

sealed class ValidationErrors(
    numberError: String,
    prefix: String = "VR-",
    override val description: String,
    val entityId: String? = null
) : Fail.Error(prefix = prefix) {

    override val code: String = prefix + numberError

    override fun logging(logger: Logger) {
        logger.error(message = message)
    }

    class InvalidOwner(val owner: Owner, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.2",
        description = "Invalid owner '$owner' by cpid '${cpid}'."
    )

    class InvalidToken(val token: Token, val cpid: Cpid) : ValidationErrors(
        numberError = "10.1.1.1",
        description = "Invalid token '$token' by cpid '$cpid'."
    )

    class LotsNotFoundSetStateForLots(val lotsId: Collection<String>) : ValidationErrors(
        numberError = "10.1.7.1",
        description = "Lots '$lotsId' do not found."
    )

    class TenderNotFoundGetLotStateByIds(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.3.1",
        description = "Tender not found by cpid '$cpid' and '$ocid'."
    )

    class LotsNotFoundGetLotStateByIds(val lotsId: Collection<String>) : ValidationErrors(
        numberError = "10.1.3.2",
        description = "Lots '$lotsId' do not found."
    )

    class TenderNotFoundCheckAccessToTender(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.1.3",
        description = "Tender not found by cpid '$cpid' and '$ocid'."
    )

    class TenderNotFoundSetStateForTender(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.6.1",
        description = "Tender not found by cpid '$cpid' and '$ocid'."
    )

    class InvalidBusinessFunctionType(id: String, allowedValues: List<String>) : ValidationErrors(
        numberError = "10.1.5.2",
        description = "Business function '${id}' has invalid type. Allowed values: ${allowedValues}"
    )

    class InvalidDocumentType(id: String, allowedValues: List<String>) : ValidationErrors(
        numberError = "10.1.5.1",
        description = "Document '${id}' has invalid type. Allowed values: ${allowedValues}"
    )

    class TenderNotFoundOnGetTenderState(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "10.1.8.1",
        description = "Tender not found by cpid '$cpid' and ocid '$ocid'."
    )

    class TenderNotFoundOnGetOrganization(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "1.9.1",
        description = "Tender not found by cpid '$cpid' and ocid '$ocid'."
    )

    class RequirementsNotFoundOnVerifyRequirementResponse(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "1.5.1",
        description = "Requirements not found by cpid '$cpid' and ocid '$ocid'."
    )

    class RequirementNotFoundOnVerifyRequirementResponse(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "1.5.2",
        description = "Requirement not found by cpid '$cpid' and ocid '$ocid'."
    )

    class InvalidCriteriaSourceOnVerifyRequirementResponse(requirement: Requirement) : ValidationErrors(
        numberError = "1.5.4",
        description = "Criteria that contains requirement (id='${requirement.id}') must have source='${CriteriaSource.PROCURING_ENTITY}'"
    )

    class RequirementDataTypeMismatchOnValidateRequirementResponse(
        id: RequirementResponseId,
        received: RequirementDataType,
        available: RequirementDataType
    ) : ValidationErrors(
        numberError = "1.5.3",
        description = "Requirement response's value with id='${id}' is mismatching with stored requirement data type. " +
            "Expected: ${available}, Actual: ${received}."
    )

    class TenderNotFoundOnGetQualificationCriteriaAndMethod(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "1.11.1",
        description = "Tender entity not found by cpid '$cpid' and ocid '$ocid'."
    )

    class RequirementDataTypeMismatchOnValidateRequirementResponses(
        id: RequirementResponseId,
        received: RequirementDataType,
        available: RequirementDataType
    ) : ValidationErrors(
        numberError = "1.10.3",
        description = "Requirement response's value with id='${id}' is mismatching with stored requirement data type. " +
            "Expected: ${available}, Actual: ${received}."
    )

    class OrganizationIdNotPassedOnValidateRequirementResponses(
        candidateId: String,
        requirementResponseId: RequirementResponseId
    ) : ValidationErrors(
        numberError = "1.10.4",
        description = "For relatedCandidate='${candidateId}' that located in requirement response with id='${requirementResponseId}' " +
            "cannot founded in 'organizationIds' array."
    )

    class TenderNotFoundOnCreateCriteriaForProcuringEntity(val cpid: Cpid, val ocid: Ocid) : ValidationErrors(
        numberError = "1.12.1",
        description = "Tender entity not found by cpid '$cpid' and ocid '$ocid'."
    )

    class TenderNotFoundOnCheckExistenceFA(cpid: Cpid, stage: Stage) : ValidationErrors(
        numberError = "10.1.21.1",
        description = "Tender not found by cpid='$cpid' and stage='$stage'."
    )

    class DuplicatedAnswerOnValidateRequirementResponses(
        val candidateId: String,
        val requirementId: RequirementId,
        val requirementResponses: List<ValidateRequirementResponsesParams.RequirementResponse>
    ) : ValidationErrors(
        numberError = "1.10.5",
        description = "Candidate (id='$candidateId') answered on requirement (id='$requirementId') multiple times " +
            "by requirement responses: $requirementResponses "
    )

    class TenderStatesNotFound(
        country: String,
        pmd: ProcurementMethod,
        operationType: OperationType
    ) : ValidationErrors(
        numberError = "17",
        description = "Tender states not found by country='$country' and pmd='${pmd.name}' and operationType='$operationType'."
    )

    class TenderNotFoundOnCheckTenderState(cpid: Cpid, ocid: Ocid) : ValidationErrors(
        numberError = "1.17.1",
        prefix = "VR.COM-",
        description = "Tender not found by cpid='$cpid' and ocid='$ocid'."
    )

    class TenderStatesIsInvalidOnCheckTenderState(tenderId: String) : ValidationErrors(
        numberError = "1.17.2",
        prefix = "VR.COM-",
        description = "Tender with id='$tenderId' has invalid states.",
        entityId = tenderId
    )

    class TenderNotFoundOnFindAuctions(cpid: Cpid, ocid: Ocid) : ValidationErrors(
        numberError = "1.19.1",
        prefix = "VR.COM-",
        description = "Tender not found by cpid='$cpid' and ocid='$ocid'."
    )

    class TenderNotFoundOnOutsourcingPN(cpid: Cpid, ocid: Ocid) : ValidationErrors(
        numberError = "1.21.1",
        prefix = "VR.COM-",
        description = "Tender not found by cpid='$cpid' and ocid='$ocid'."
    )

    class TenderNotFoundOnCheckRelation(cpid: Cpid, ocid: Ocid) : ValidationErrors(
        numberError = "1.24.1",
        prefix = "VR.COM-",
        description = "Tender not found by cpid='$cpid' and ocid='$ocid'."
    )

    class TenderNotFoundOnCreateRelationToOtherProcess(cpid: Cpid, ocid: Ocid) : ValidationErrors(
        numberError = "1.22.1",
        prefix = "VR.COM-",
        description = "Tender not found by cpid='$cpid' and ocid='$ocid'."
    )
}
