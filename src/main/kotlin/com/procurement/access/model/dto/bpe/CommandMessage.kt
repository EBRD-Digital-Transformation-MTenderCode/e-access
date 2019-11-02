package com.procurement.access.model.dto.bpe

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.exception.EnumException
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.utils.toLocal
import java.time.LocalDateTime
import java.util.*

data class CommandMessage @JsonCreator constructor(

    val id: String,
    val command: CommandType,
    val context: Context,
    val data: JsonNode,
    val version: ApiVersion
)

val CommandMessage.cpid: String
    get() = this.context.cpid
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'cpid' attribute in context.")

val CommandMessage.token: UUID
    get() = this.context.token?.let { id ->
        try {
            UUID.fromString(id)
        } catch (exception: Exception) {
            throw ErrorException(error = ErrorType.INVALID_FORMAT_TOKEN)
        }
    } ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'token' attribute in context.")

val CommandMessage.owner: String
    get() = this.context.owner
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'owner' attribute in context.")

val CommandMessage.stage: String
    get() = this.context.stage
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'stage' attribute in context.")

val CommandMessage.prevStage: String
    get() = this.context.prevStage
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'prevStage' attribute in context.")

val CommandMessage.country: String
    get() = this.context.country
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'country' attribute in context.")

val CommandMessage.pmd: ProcurementMethod
    get() = this.context.pmd?.let {
        ProcurementMethod.valueOrException(it) {
            ErrorException(ErrorType.INVALID_PMD)
        }
    } ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'pmd' attribute in context.")

val CommandMessage.operationType: OperationType
    get() = this.context.operationType?.let { OperationType.fromString(it) }
        ?: throw ErrorException(
            error = ErrorType.CONTEXT,
            message = "Missing the 'operationType' attribute in context."
        )

val CommandMessage.startDate: LocalDateTime
    get() = this.context.startDate?.toLocal()
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'startDate' attribute in context.")

val CommandMessage.testMode: Boolean
    get() = this.context.testMode?.let { it } ?: false

val CommandMessage.isAuction: Boolean
    get() = this.context.isAuction?.let { it } ?: false

val CommandMessage.lotId: LotId
    get() = this.context.id?.let { id ->
        try {
            LotId.fromString(id)
        } catch (exception: Exception) {
            throw ErrorException(error = ErrorType.INVALID_FORMAT_LOT_ID)
        }
    } ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'id' attribute in context.")

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
    @get:JsonProperty("isAuction") @param:JsonProperty("isAuction") val isAuction: Boolean?,
    val id: String?,
    val testMode: Boolean?
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
    CHECK_CN_ON_PN("checkCnOnPn"),

    SET_TENDER_SUSPENDED("setTenderSuspended"),
    SET_TENDER_UNSUSPENDED("setTenderUnsuspended"),
    SET_TENDER_UNSUCCESSFUL("setTenderUnsuccessful"),
    SET_TENDER_PRECANCELLATION("setTenderPreCancellation"),
    SET_TENDER_CANCELLATION("setTenderCancellation"),
    SET_TENDER_STATUS_DETAILS("setTenderStatusDetails"),
    GET_TENDER_OWNER("getTenderOwner"),
    START_NEW_STAGE("startNewStage"),

    GET_ITEMS_BY_LOT("getItemsByLot"),
    GET_LOTS("getLots"),
    GET_LOT("getLot"),
    GET_LOTS_AUCTION("getLotsAuction"),
    GET_AWARD_CRITERIA("getAwardCriteria"),
    GET_DATA_FOR_AC("getDataForAc"),
    SET_LOTS_SD_UNSUCCESSFUL("setLotsStatusDetailsUnsuccessful"),
    SET_LOTS_SD_AWARDED("setLotsStatusDetailsAwarded"),
    SET_LOTS_UNSUCCESSFUL("setLotsStatusUnsuccessful"),
    SET_FINAL_STATUSES("setFinalStatuses"),
    COMPLETE_LOTS("completeLots"),
    SET_LOTS_INITIAL_STATUS("setLotInitialStatus"),

    CHECK_AWARD("checkAward"),
    CHECK_LOT_ACTIVE("checkLotActive"),
    CHECK_LOT_STATUS("checkLotStatus"),
    CHECK_LOTS_STATUS("checkLotsStatus"),
    CHECK_LOT_AWARDED("checkLotAwarded"),
    CHECK_BID("checkBid"),
    CHECK_ITEMS("checkItems"),
    CHECK_TOKEN("checkToken"),
    CHECK_BUDGET_SOURCES("checkBudgetSources"),

    VALIDATE_OWNER_AND_TOKEN("validateOwnerAndToken"),
    GET_LOTS_FOR_AUCTION("getLotsForAuction");

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
        errors = listOf(
            ResponseErrorDto(
                code = "400.03.00",
                description = exception.message ?: exception.toString()
            )
        )
    )
}

fun getErrorExceptionResponseDto(exception: ErrorException, id: String? = null): ResponseDto {
    return ResponseDto(
        errors = listOf(
            ResponseErrorDto(
                code = "400.03." + exception.error.code,
                description = exception.message
            )
        ),
        id = id
    )
}

fun getEnumExceptionResponseDto(error: EnumException, id: String? = null): ResponseDto {
    return ResponseDto(
        errors = listOf(
            ResponseErrorDto(
                code = "400.03." + error.code,
                description = error.msg
            )
        ),
        id = id
    )
}
