package com.procurement.access.infrastructure.api.v2

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.infrastructure.api.Action

enum class CommandTypeV2(@JsonValue override val key: String) : EnumElementProvider.Key, Action {

    ADD_CLIENTS_TO_PARTIES_IN_AP("addClientsToPartiesInAP"),
    CALCULATE_AP_VALUE("calculateAPValue"),
    CHECK_ACCESS_TO_TENDER("checkAccessToTender"),
    CHECK_EQUALITY_CURRENCIES("checkEqualityCurrencies"),
    CHECK_EXISTENCE_FA("checkExistenceFA"),
    CHECK_EXISTENCE_SIGN_AUCTION("checkExistenceSignAuction"),
    CHECK_LOTS_STATE("checkLotsState"),
    CHECK_PERSONES_STRUCTURE("checkPersonesStructure"),
    CHECK_RELATION("checkRelation"),
    CHECK_TENDER_STATE("checkTenderState"),
    CREATE_CRITERIA_FOR_PROCURING_ENTITY("createCriteriaForProcuringEntity"),
    CREATE_RELATION_TO_CONTRACT_PROCESS_STAGE("createRelationToContractProcessStage"),
    CREATE_RELATION_TO_OTHER_PROCESS("createRelationToOtherProcess"),
    CREATE_RFQ("createRfq"),
    DEFINE_TENDER_CLASSIFICATION("defineTenderClassification"),
    DIVIDE_LOT("divideLot"),
    FIND_AUCTIONS("findAuctions"),
    FIND_CRITERIA("findCriteria"),
    FIND_LOT_IDS("findLotIds"),
    GET_BUYERS_OWNERS("getBuyersOwners"),
    GET_CURRENCY("getCurrency"),
    GET_DATA_FOR_CONTRACT("getDataForContract"),
    GET_ITEMS_BY_LOT_IDS("getItemsByLotIds"),
    GET_LOTS_VALUE("getLotsValue"),
    GET_LOT_STATE_BY_IDS("getLotStateByIds"),
    GET_MAIN_PROCUREMENT_CATEGORY("getMainProcurementCategory"),
    GET_ORGANIZATIONS("getOrganizations"),
    GET_QUALIFICATION_CRITERIA_AND_METHOD("getQualificationCriteriaAndMethod"),
    GET_TENDER_STATE("getTenderState"),
    OUTSOURCING_PN("outsourcingPN"),
    PERSONES_PROCESSING("personesProcessing"),
    RESPONDER_PROCESSING("responderProcessing"),
    SET_STATE_FOR_LOTS("setStateForLots"),
    SET_STATE_FOR_TENDER("setStateForTender"),
    VALIDATE_CLASSIFICATION("validateClassification"),
    VALIDATE_LOTS_DATA_FOR_DIVISION("validateLotsDataForDivision"),
    VALIDATE_REQUIREMENT_RESPONSES("validateRequirementResponses"),
    VALIDATE_RFQ_DATA("ValidateRfqData"),
    VERIFY_REQUIREMENT_RESPONSE("verifyRequirementResponse"),
    ;

    override fun toString(): String = key

    companion object : EnumElementProvider<CommandTypeV2>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CommandTypeV2.orThrow(name)
    }
}