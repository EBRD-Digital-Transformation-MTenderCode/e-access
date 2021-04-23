package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.params.CheckLotsStateParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseLotId
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parseOperationType
import com.procurement.access.application.model.parsePmd
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.infrastructure.handler.v2.model.request.CheckLotsStateRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

val allowedPmd = ProcurementMethod.values()
    .filter {
        when (it) {
            ProcurementMethod.DA, ProcurementMethod.TEST_DA,
            ProcurementMethod.DC, ProcurementMethod.TEST_DC,
            ProcurementMethod.NP, ProcurementMethod.TEST_NP,
            ProcurementMethod.CD, ProcurementMethod.TEST_CD,
            ProcurementMethod.IP, ProcurementMethod.TEST_IP,
            ProcurementMethod.GPA, ProcurementMethod.TEST_GPA,
            ProcurementMethod.RT, ProcurementMethod.TEST_RT,
            ProcurementMethod.RFQ, ProcurementMethod.TEST_RFQ,
            ProcurementMethod.OT, ProcurementMethod.TEST_OT,
            ProcurementMethod.SV, ProcurementMethod.TEST_SV,
            ProcurementMethod.MV, ProcurementMethod.TEST_MV -> true

            ProcurementMethod.OP, ProcurementMethod.TEST_OP,
            ProcurementMethod.MC, ProcurementMethod.TEST_MC,
            ProcurementMethod.DCO, ProcurementMethod.TEST_DCO,
            ProcurementMethod.CF, ProcurementMethod.TEST_CF,
            ProcurementMethod.OF, ProcurementMethod.TEST_OF,
            ProcurementMethod.FA, ProcurementMethod.TEST_FA -> false
        }
    }.toSet()

val allowedOperationType = OperationType.allowedElements
    .filter {
        when (it) {
            OperationType.AWARD_CONSIDERATION,
            OperationType.CREATE_AWARD,
            OperationType.DECLARE_NON_CONFLICT_OF_INTEREST,
            OperationType.DIVIDE_LOT,
            OperationType.SUBMIT_BID,
            OperationType.UPDATE_AWARD -> true

            OperationType.AMEND_FE,
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
            OperationType.CREATE_RFQ,
            OperationType.CREATE_SUBMISSION,
            OperationType.ISSUING_FRAMEWORK_CONTRACT,
            OperationType.OUTSOURCING_PN,
            OperationType.QUALIFICATION,
            OperationType.QUALIFICATION_CONSIDERATION,
            OperationType.QUALIFICATION_DECLARE_NON_CONFLICT_OF_INTEREST,
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

fun CheckLotsStateRequest.convert(): Result<CheckLotsStateParams, DataErrors> {
    val cpidParsed = parseCpid(cpid).onFailure { return it }
    val ocidParsed = parseOcid(ocid).onFailure { return it }
    val pmdParsed = parsePmd(pmd, allowedPmd).onFailure { return it }
    val operationTypeParsed = parseOperationType(operationType, allowedOperationType).onFailure { return it }
    val tender = tender.convert().onFailure { return it }

    return CheckLotsStateParams(
        cpid = cpidParsed,
        ocid = ocidParsed,
        pmd = pmdParsed,
        operationType = operationTypeParsed,
        country = country,
        tender = tender
    ).asSuccess()
}

fun CheckLotsStateRequest.Tender.convert(): Result<CheckLotsStateParams.Tender, DataErrors> {
    val lots = lots.mapResult { parseLotId(it.id, "tender.lots.id") }
        .onFailure { return it }
        .map { lotId -> CheckLotsStateParams.Tender.Lot(lotId) }

    return CheckLotsStateParams.Tender(lots).asSuccess()
}