package com.procurement.access.infrastructure.api.v2

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.infrastructure.api.Action

enum class CommandTypeV2(@JsonValue override val key: String) : EnumElementProvider.Key, Action {

    CALCULATE_AP_VALUE("calculateAPValue"),
    CHECK_ACCESS_TO_TENDER("checkAccessToTender"),
    CHECK_EQUALITY_CURRENCIES("checkEqualityCurrencies"),
    CHECK_EXISTENCE_FA("checkExistenceFA"),
    CHECK_EXISTENCE_SIGN_AUCTION("checkExistenceSignAuction"),
    CHECK_PERSONES_STRUCTURE("checkPersonesStructure"),
    CHECK_RELATION("checkRelation"),
    CHECK_TENDER_STATE("checkTenderState"),
    CREATE_CRITERIA_FOR_PROCURING_ENTITY("createCriteriaForProcuringEntity"),
    CREATE_RELATION_TO_OTHER_PROCESS("createRelationToOtherProcess"),
    FIND_AUCTIONS("findAuctions"),
    FIND_CRITERIA("findCriteria"),
    FIND_LOT_IDS("findLotIds"),
    GET_LOTS_VALUE("getLotsValue"),
    GET_CURRENCY("getCurrency"),
    GET_LOT_STATE_BY_IDS("getLotStateByIds"),
    GET_ORGANIZATION("getOrganization"),
    GET_QUALIFICATION_CRITERIA_AND_METHOD("getQualificationCriteriaAndMethod"),
    GET_MAIN_PROCUREMENT_CATEGORY("getMainProcurementCategory"),
    GET_TENDER_STATE("getTenderState"),
    OUTSOURCING_PN("outsourcingPN"),
    RESPONDER_PROCESSING("responderProcessing"),
    SET_STATE_FOR_LOTS("setStateForLots"),
    SET_STATE_FOR_TENDER("setStateForTender"),
    VALIDATE_CLASSIFICATION("validateClassification"),
    VALIDATE_REQUIREMENT_RESPONSES("validateRequirementResponses"),
    VERIFY_REQUIREMENT_RESPONSE("verifyRequirementResponse"),
    ;

    override fun toString(): String = key

    companion object : EnumElementProvider<CommandTypeV2>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CommandTypeV2.orThrow(name)
    }
}