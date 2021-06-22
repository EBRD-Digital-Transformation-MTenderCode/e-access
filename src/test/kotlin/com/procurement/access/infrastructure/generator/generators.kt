package com.procurement.access.infrastructure.generator

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v1.CommandMessage
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.api.v1.Context
import com.procurement.access.model.entity.TenderProcessEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object CommandMessageGenerator {
    val COMMAND_ID = CommandId(UUID.randomUUID().toString())
    val COMMAND_VERSION: ApiVersion = ApiVersion(1, 0, 0)

    fun generate(
        id: CommandId = COMMAND_ID,
        version: ApiVersion = COMMAND_VERSION,
        command: CommandTypeV1,
        context: Context,
        data: JsonNode
    ): CommandMessage {
        return CommandMessage(id = id, version = version, command = command, context = context, data = data)
    }
}

object ContextGenerator {
    const val CPID = "cpid-1"
    const val OCID = "ocds-b3wdp1-MD-1580458690892-EV-1580458791896"
    val TOKEN: UUID = UUID.fromString("bd56490f-57ca-4d1a-9210-250cb9b4eed3")
    const val OWNER = "owner-1"
    const val COUNTRY = "MD"
    const val START_DATE = "2011-06-05T17:59:00Z"

    fun generate(
        cpid: String? = CPID,
        ocid: String? = OCID,
        token: String? = TOKEN.toString(),
        owner: String? = OWNER,
        requestId: String? = null,
        operationId: String? = null,
        processType: String? = null,
        operationType: String? = null,
        phase: String? = null,
        country: String? = COUNTRY,
        language: String? = null,
        pmd: String? = null,
        startDate: String? = START_DATE,
        endDate: String? = null,
        id: String? = null
    ): Context {
        return Context(
            id = id,
            operationId = operationId,
            requestId = requestId,
            cpid = cpid,
            ocid = ocid,
            processType = processType,
            operationType = operationType,
            phase = phase,
            owner = owner,
            country = country,
            language = language,
            pmd = pmd,
            token = token,
            startDate = startDate,
            endDate = endDate,
            testMode = false,
            isAuction = false
        )
    }
}

object TenderProcessEntityGenerator {
    fun generate(
        cpid: String = ContextGenerator.CPID,
        token: UUID = ContextGenerator.TOKEN,
        owner: String = ContextGenerator.OWNER,
        ocid: String = ContextGenerator.OCID,
        createdDate: LocalDateTime = nowDefaultUTC(),
        data: String
    ): TenderProcessEntity {
        return TenderProcessEntity(
            cpId = cpid,
            token = token,
            owner = owner,
            ocid = ocid,
            createdDate = createdDate,
            jsonData = data
        )
    }

    private fun LocalDate.toDate(): Date = Date.from(
        this.atStartOfDay()
            .atZone(ZoneId.systemDefault())
            .toInstant()
    )
}
