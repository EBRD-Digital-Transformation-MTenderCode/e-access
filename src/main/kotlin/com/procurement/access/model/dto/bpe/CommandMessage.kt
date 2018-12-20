package com.procurement.access.model.bpe

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.exception.EnumException
import com.procurement.access.exception.ErrorException

data class CommandMessage @JsonCreator constructor(

        val id: String,
        val command: CommandType,
        val context: Context,
        val data: JsonNode,
        val version: ApiVersion
)

data class Context @JsonCreator constructor(
        val operationId: String?,
        val requestId: String?,
        val cpid: String?,
        val ocid: String?,
        val stage: String?,
        val prevStage: String?,
        val processType: String?,
        val operationType: String?,
        val phase: String?,
        val owner: String?,
        val country: String?,
        val language: String?,
        val pmd: String?,
        val token: String?,
        val startDate: String?,
        val endDate: String?,
        val id: String?
)

enum class CommandType(private val value: String) {

    CREATE_PIN("createPin"),
    CREATE_PN("createPn"),
    UPDATE_PN("updatePn"),
    CREATE_CN("createCn"),
    UPDATE_CN("updateCn"),
    CREATE_PIN_ON_PN("createPinOnPn"),
    CREATE_CN_ON_PIN("createCnOnPin"),
    CREATE_CN_ON_PN("createCnOnPn"),

    SET_TENDER_SUSPENDED("setTenderSuspended"),
    SET_TENDER_UNSUSPENDED("setTenderUnsuspended"),
    SET_TENDER_UNSUCCESSFUL("setTenderUnsuccessful"),
    SET_TENDER_PRECANCELLATION("setTenderPreCancellation"),
    SET_TENDER_CANCELLATION("setTenderCancellation"),
    SET_TENDER_STATUS_DETAILS("setTenderStatusDetails"),
    GET_TENDER_OWNER("getTenderOwner"),
    START_NEW_STAGE("startNewStage"),

    GET_LOTS("getLots"),
    GET_LOTS_AUCTION("getLotsAuction"),
    GET_AWARD_CRITERIA("getAwardCriteria"),
    SET_LOTS_SD_UNSUCCESSFUL("setLotsStatusDetailsUnsuccessful"),
    SET_LOTS_SD_AWARDED("setLotsStatusDetailsAwarded"),
    SET_LOTS_UNSUCCESSFUL("setLotsStatusUnsuccessful"),
    SET_LOTS_UNSUCCESSFUL_EV("setLotsStatusUnsuccessfulEv"),
    COMPLETE_LOT("completeLot"),
    SET_LOTS_INITIAL_STATUS("setLotInitialStatus"),

    CHECK_LOT_STATUS_AND_GET_ITEMS("checkLotStatusAndGetItems"),
    CHECK_LOTS_STATUS("checkLotsStatus"),
    CHECK_BID("checkBid"),
    CHECK_ITEMS("checkItems"),
    CHECK_TOKEN("checkToken"),
    CHECK_BUDGET_SOURCES("checkBudgetSources"),
    VALIDATE_OWNER_AND_TOKEN("validateOwnerAndToken");


    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }
}

enum class ApiVersion(private val value: String) {
    V_0_0_1("0.0.1");

    @JsonValue
    fun value(): String {
        return this.value
    }

    override fun toString(): String {
        return this.value
    }
}


@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseDto(

        val errors: List<ResponseErrorDto>? = null,

        val data: Any? = null,

        val id: String? = null
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseErrorDto(

        val code: String,

        val description: String?
)

fun getExceptionResponseDto(exception: Exception): ResponseDto {
    return ResponseDto(
            errors = listOf(ResponseErrorDto(
                    code = "400.03.00",
                    description = exception.message ?: exception.toString()
            )))
}

fun getErrorExceptionResponseDto(error: ErrorException, id: String? = null): ResponseDto {
    return ResponseDto(
            errors = listOf(ResponseErrorDto(
                    code = "400.03." + error.code,
                    description = error.msg
            )),
            id = id)
}

fun getEnumExceptionResponseDto(error: EnumException, id: String? = null): ResponseDto {
    return ResponseDto(
            errors = listOf(ResponseErrorDto(
                    code = "400.03." + error.code,
                    description = error.msg
            )),
            id = id)
}

