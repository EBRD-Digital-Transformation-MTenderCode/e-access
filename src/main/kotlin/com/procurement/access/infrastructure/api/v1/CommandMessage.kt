package com.procurement.access.infrastructure.api.v1

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.domain.util.extension.toLocalDateTime
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import java.time.LocalDateTime
import java.util.*

data class CommandMessage @JsonCreator constructor(
    val id: CommandId,
    val command: CommandTypeV1,
    val context: Context,
    val data: JsonNode,
    val version: ApiVersion
)

val CommandMessage.commandId: CommandId
    get() = this.id

val CommandMessage.cpid: String
    get() = this.context.cpid
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'cpid' attribute in context.")

val CommandMessage.cpidParsed: Cpid
    get() = Cpid.tryCreate(cpid)
        .orThrow { _ ->
            ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "Attribute 'cpid' has invalid format."
            )
        }

val CommandMessage.ocid: String
    get() = this.context.ocid
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'ocid' attribute in context.")

val CommandMessage.ocidParsed: Ocid.SingleStage
    get() = Ocid.SingleStage.tryCreate(ocid)
        .orThrow { _ ->
            ErrorException(
                error = ErrorType.INCORRECT_VALUE_ATTRIBUTE,
                message = "Attribute 'ocid' has invalid format."
            )
        }

val CommandMessage.token: UUID
    get() = this.context
        .token
        ?.let { token ->
            try {
                UUID.fromString(token)
            } catch (exception: Exception) {
                throw ErrorException(
                    error = ErrorType.INVALID_FORMAT_TOKEN,
                    message = "Expected token format is UUID, actual token=$token."
                )
            }
        }
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'token' attribute in context.")

val CommandMessage.owner: String
    get() = this.context.owner
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'owner' attribute in context.")

val CommandMessage.stage: Stage
    get() = this.context.stage
        ?.let { Stage.creator(it) }
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'stage' attribute in context.")

val CommandMessage.prevStage: String
    get() = this.context.prevStage
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'prevStage' attribute in context.")

val CommandMessage.country: String
    get() = this.context.country
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'country' attribute in context.")

val CommandMessage.phase: String
    get() = this.context.phase
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'phase' attribute in context.")

val CommandMessage.pmd: ProcurementMethod
    get() = this.context.pmd?.let {
        ProcurementMethod.creator(it)
    } ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'pmd' attribute in context.")

val CommandMessage.operationType: OperationType
    get() = this.context.operationType?.let { OperationType.creator(it) }
        ?: throw ErrorException(
            error = ErrorType.CONTEXT,
            message = "Missing the 'operationType' attribute in context."
        )

val CommandMessage.startDate: LocalDateTime
    get() = this.context.startDate
        ?.toLocalDateTime()
        ?.orThrow { it.reason }
        ?: throw ErrorException(error = ErrorType.CONTEXT, message = "Missing the 'startDate' attribute in context.")

val CommandMessage.testMode: Boolean
    get() = this.context.testMode ?: false

val CommandMessage.isAuction: Boolean
    get() = this.context.isAuction ?: false

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
