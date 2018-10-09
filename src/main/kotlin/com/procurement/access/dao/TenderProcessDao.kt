package com.procurement.access.dao

import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder.*
import com.procurement.access.model.entity.TenderProcessEntity
import org.springframework.stereotype.Service

@Service
class TenderProcessDao(private val session: Session) {

    fun save(entity: TenderProcessEntity) {
        val insert = insertInto(TENDER_TABLE)
        insert.value(CP_ID, entity.cpId)
                .value(TOKEN, entity.token)
                .value(OWNER, entity.owner)
                .value(STAGE, entity.stage)
                .value(CREATED_DATE, entity.createdDate)
                .value(JSON_DATA, entity.jsonData)
        session.execute(insert)
    }

    fun getByCpIdAndStage(cpId: String, stage: String): TenderProcessEntity? {
        val query = select()
                .all()
                .from(TENDER_TABLE)
                .where(eq(CP_ID, cpId))
                .and(eq(STAGE, stage)).limit(1)
        val row = session.execute(query).one()
        return if (row != null) TenderProcessEntity(
                row.getString(CP_ID),
                row.getUUID(TOKEN),
                row.getString(OWNER),
                row.getString(STAGE),
                row.getTimestamp(CREATED_DATE),
                row.getString(JSON_DATA)) else null

    }

    companion object {
        private const val TENDER_TABLE = "access_tender"
        private const val CP_ID = "cp_id"
        private const val TOKEN = "token_entity"
        private const val STAGE = "stage"
        private const val CREATED_DATE = "created_date"
        private const val OWNER = "owner"
        private const val JSON_DATA = "json_data"
    }
}
