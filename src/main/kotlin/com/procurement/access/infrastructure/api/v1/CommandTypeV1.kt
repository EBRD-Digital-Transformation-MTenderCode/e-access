package com.procurement.access.infrastructure.api.v1

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider
import com.procurement.access.infrastructure.api.Action

enum class CommandTypeV1(@JsonValue override val key: String) : EnumElementProvider.Key, Action {

    AMEND_FE("amendFE"),
    CHECK_AWARD("checkAward"),
    CHECK_BID("checkBid"),
    CHECK_BUDGET_SOURCES("checkBudgetSources"),
    CHECK_CN_ON_PN("checkCnOnPn"),
    CHECK_EXISTANCE_ITEMS_AND_LOTS("checkExistenceItemsAndLots"),
    CHECK_FE_DATA("checkFEData"),
    CHECK_ITEMS("checkItems"),
    CHECK_LOTS_STATUS("checkLotsStatus"),
    CHECK_LOT_ACTIVE("checkLotActive"),
    CHECK_LOT_AWARDED("checkLotAwarded"),
    CHECK_LOT_STATUS("checkLotStatus"),
    CHECK_RESPONSES("checkResponses"),
    CHECK_TOKEN("checkToken"),
    COMPLETE_LOTS("completeLots"),
    CREATE_AP("createAp"),
    CREATE_CN("createCn"),
    CREATE_CN_ON_PIN("createCnOnPin"),
    CREATE_CN_ON_PN("createCnOnPn"),
    CREATE_FE("createFE"),
    CREATE_PIN("createPin"),
    CREATE_PIN_ON_PN("createPinOnPn"),
    CREATE_PN("createPn"),
    CREATE_REQUESTS_FOR_EV_PANELS("createRequestsForEvPanels"),
    GET_ACTIVE_LOTS("getActiveLots"),
    GET_AP_TITLE_AND_DESCRIPTION("getAPTitleAndDescription"),
    GET_AWARD_CRITERIA("getAwardCriteria"),
    GET_AWARD_CRITERIA_AND_CONVERSIONS("getAwardCriteriaAndConversions"),
    GET_CRITERIA_FOR_TENDERER("getCriteriaForTenderer"),
    GET_DATA_FOR_AC("getDataForAc"),
    GET_ITEMS_BY_LOT("getItemsByLot"),
    GET_ITEMS_BY_LOTS("getItemsByLots"),
    GET_LOT("getLot"),
    GET_LOTS_AUCTION("getLotsAuction"),
    GET_LOTS_FOR_AUCTION("getLotsForAuction"),
    GET_MAIN_PROCUREMENT_CATEGORY("getMainProcurementCategory"),
    GET_TENDER_OWNER("getTenderOwner"),
    SET_FINAL_STATUSES("setFinalStatuses"),
    SET_LOTS_INITIAL_STATUS("setLotInitialStatus"),
    SET_LOTS_SD_AWARDED("setLotsStatusDetailsAwarded"),
    SET_LOTS_SD_UNSUCCESSFUL("setLotsStatusDetailsUnsuccessful"),
    SET_LOTS_UNSUCCESSFUL("setLotsStatusUnsuccessful"),
    SET_TENDER_CANCELLATION("setTenderCancellation"),
    SET_TENDER_PRECANCELLATION("setTenderPreCancellation"),
    SET_TENDER_STATUS_DETAILS("setTenderStatusDetails"),
    SET_TENDER_SUSPENDED("setTenderSuspended"),
    SET_TENDER_UNSUCCESSFUL("setTenderUnsuccessful"),
    SET_TENDER_UNSUSPENDED("setTenderUnsuspended"),
    START_NEW_STAGE("startNewStage"),
    UPDATE_AP("updateAp"),
    UPDATE_CN("updateCn"),
    UPDATE_PN("updatePn"),
    VALIDATE_OWNER_AND_TOKEN("validateOwnerAndToken"),
    ;

    override fun toString(): String = key

    companion object : EnumElementProvider<CommandTypeV1>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = CommandTypeV1.orThrow(name)
    }
}
