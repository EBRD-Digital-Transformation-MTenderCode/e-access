package com.procurement.access.dao

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder.eq
import com.datastax.driver.core.querybuilder.QueryBuilder.insertInto
import com.datastax.driver.core.querybuilder.QueryBuilder.select
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.infrastructure.api.v2.ApiResponseV2
import com.procurement.access.infrastructure.extension.cassandra.toLocalDateTime
import com.procurement.access.model.dto.bpe.ResponseDto
import com.procurement.access.model.entity.HistoryEntity
import com.procurement.access.utils.toJson
import org.springframework.stereotype.Service

@Service
class HistoryDao(private val session: Session) {

    fun getHistory(operationId: String, command: String): HistoryEntity? {
        val query = select()
                .all()
                .from(HISTORY_TABLE)
                .where(eq(OPERATION_ID, operationId))
                .and(eq(COMMAND, command))
                .limit(1)
        val row = session.execute(query).one()
        return if (row != null) HistoryEntity(
            row.getString(OPERATION_ID),
            row.getString(COMMAND),
            row.getTimestamp(OPERATION_DATE).toLocalDateTime(),
            row.getString(JSON_DATA)) else null
    }

    fun saveHistory(operationId: String, command: String, response: ResponseDto): HistoryEntity {
        val entity = HistoryEntity(
            operationId = operationId,
            command = command,
            operationDate = nowDefaultUTC(),
            jsonData = toJson(response))

        val insert = insertInto(HISTORY_TABLE)
                .value(OPERATION_ID, entity.operationId)
                .value(COMMAND, entity.command)
                .value(OPERATION_DATE, entity.operationDate)
                .value(JSON_DATA, entity.jsonData)
        session.execute(insert)
        return entity
    }

    fun saveHistory(operationId: String, command: String, response: ApiResponseV2): HistoryEntity {
        val entity = HistoryEntity(
            operationId = operationId,
            command = command,
            operationDate = nowDefaultUTC(),
            jsonData = toJson(response))

        val insert = insertInto(HISTORY_TABLE)
            .value(OPERATION_ID, entity.operationId)
            .value(COMMAND, entity.command)
            .value(OPERATION_DATE, entity.operationDate)
            .value(JSON_DATA, entity.jsonData)
        session.execute(insert)
        return entity
    }

    companion object {
        private const val HISTORY_TABLE = "access_history"
        private const val OPERATION_ID = "operation_id"
        private const val COMMAND = "command"
        private const val OPERATION_DATE = "operation_date"
        private const val JSON_DATA = "json_data"
    }

}
