package com.procurement.access.infrastructure.generator

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.model.dto.bpe.ApiVersion
import com.procurement.access.model.dto.bpe.CommandMessage
import com.procurement.access.model.dto.bpe.CommandType
import com.procurement.access.model.dto.bpe.Context
import com.procurement.access.model.dto.ocds.Operation
import com.procurement.access.model.entity.TenderProcessEntity
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

object TestDataGenerator {

    const val CPID = "cpid-1"
    const val STAGE = "stage"
    const val PREV_STAGE = "prev_stage"
    const val START_DATE = "2011-06-05T17:59:00Z"
    val TOKEN: UUID = UUID.fromString("bd56490f-57ca-4d1a-9210-250cb9b4eed3")
    const val OWNER = "owner-1"
    const val COUNTRY = "MD"
    const val COMMAND_ID = "COMMAND_ID"

    fun commandMessage(
        version: ApiVersion = ApiVersion.V_0_0_1,
        id: String = COMMAND_ID,
        command: CommandType,
        cpid: String = CPID,
        pmd: String,
        token: String = TOKEN.toString(),
        owner: String = OWNER,
        stage: String = STAGE,
        prevStage: String = PREV_STAGE,
        startDate: String = START_DATE,
        country: String = COUNTRY,
        operationType: Operation,
        data: JsonNode
    ): CommandMessage {
        return CommandMessage(
            version = version,
            id = id,
            command = command,
            context = context(
                cpid = cpid,
                token = token,
                owner = owner,
                stage = stage,
                prevStage = prevStage,
                startDate = startDate,
                country = country,
                operationType = operationType.value,
                pmd = pmd
            ),
            data = data
        )
    }

    fun context(
        id: String? = null,
        operationId: String? = null,
        requestId: String? = null,
        cpid: String? = null,
        ocid: String? = null,
        stage: String? = null,
        prevStage: String? = null,
        processType: String? = null,
        operationType: String? = null,
        phase: String? = null,
        owner: String? = null,
        country: String? = null,
        language: String? = null,
        pmd: String? = null,
        token: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ): Context {
        return Context(
            id = id,
            operationId = operationId,
            requestId = requestId,
            cpid = cpid,
            ocid = ocid,
            stage = stage,
            prevStage = prevStage,
            processType = processType,
            operationType = operationType,
            phase = phase,
            owner = owner,
            country = country,
            language = language,
            pmd = pmd,
            token = token,
            startDate = startDate,
            endDate = endDate
        )
    }

    fun tenderProcessEntity(
        cpid: String = CPID,
        token: UUID = TOKEN,
        owner: String = OWNER,
        stage: String = STAGE,
        createdDate: Date = LocalDate.now().toDate(),
        data: String
    ): TenderProcessEntity {
        return TenderProcessEntity(
            cpId = cpid,
            token = token,
            owner = owner,
            stage = stage,
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

