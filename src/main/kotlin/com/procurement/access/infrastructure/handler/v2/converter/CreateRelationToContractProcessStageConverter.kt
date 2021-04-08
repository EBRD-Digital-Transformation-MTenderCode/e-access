package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.params.CreateRelationToContractProcessStageParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseOperationType
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRelationToContractProcessStageRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.asSuccess

fun CreateRelationToContractProcessStageRequest.convert(): Result<CreateRelationToContractProcessStageParams, DataErrors> {
    val parsedOcid = Ocid.SingleStage.tryCreateOrNull(value = ocid)
        ?: Ocid.MultiStage.tryCreateOrNull(value = ocid)
        ?: return failure(
            DataErrors.Validation.DataMismatchToPattern(
                name = "ocid",
                pattern = "${Ocid.SingleStage.pattern}, ${Ocid.MultiStage.pattern} ",
                actualValue = ocid
            )
        )

    return CreateRelationToContractProcessStageParams(
        cpid = parseCpid(cpid).onFailure { return it },
        ocid = parsedOcid,
        relatedOcid = parseOcid(relatedOcid).onFailure { return it },
        operationType = parseOperationType(operationType, allowedOperationTypes).onFailure { return it }
    ).asSuccess()
}

private val allowedOperationTypes = OperationType.values()
    .filter {
        when (it) {
            OperationType.CREATE_RFQ -> true
            OperationType.UPDATE_AWARD,
            OperationType.SUBMIT_BID,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.DIVIDE_LOT,
            OperationType.CREATE_AWARD,
            OperationType.AWARD_CONSIDERATION,
            OperationType.AMEND_FE,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.APPLY_QUALIFICATION_PROTOCOL,
            OperationType.COMPLETE_QUALIFICATION,
            OperationType.CREATE_CN,
            OperationType.CREATE_CN_ON_PIN,
            OperationType.CREATE_CN_ON_PN,
            OperationType.CREATE_FE,
            OperationType.CREATE_NEGOTIATION_CN_ON_PN,
            OperationType.CREATE_PCR,
            OperationType.CREATE_PIN,
            OperationType.CREATE_PIN_ON_PN,
            OperationType.CREATE_PN,
            OperationType.CREATE_SUBMISSION,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_PROTOCOL,
            OperationType.RELATION_AP,
            OperationType.START_SECONDSTAGE,
            OperationType.SUBMISSION_PERIOD_END,
            OperationType.TENDER_PERIOD_END,
            OperationType.UPDATE_AP,
            OperationType.UPDATE_CN,
            OperationType.UPDATE_PN,
            OperationType.WITHDRAW_BID,
            OperationType.WITHDRAW_QUALIFICATION_PROTOCOL -> false
        }
    }.toSet()
