package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.util.extension.nowDefaultUTC
import com.procurement.access.infrastructure.api.Action
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.extension.cassandra.toCassandraTimestamp
import com.procurement.access.infrastructure.extension.cassandra.tryExecute
import com.procurement.access.infrastructure.handler.HistoryRepositoryOld
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository

@Repository
class CassandraHistoryRepositoryV1(@Qualifier("ocds") private val session: Session) : HistoryRepositoryOld {

    companion object {

        private const val SAVE_HISTORY_CQL = """
               INSERT INTO ${Database.KEYSPACE_OLD}.${Database.HistoryOld.TABLE}(
                      ${Database.HistoryOld.COMMAND_ID},
                      ${Database.HistoryOld.COMMAND_NAME},
                      ${Database.HistoryOld.COMMAND_DATE},
                      ${Database.HistoryOld.JSON_DATA}
               )
               VALUES(?, ?, ?, ?)
               IF NOT EXISTS
            """

        private const val FIND_HISTORY_ENTRY_CQL = """
               SELECT ${Database.HistoryOld.COMMAND_ID},
                      ${Database.HistoryOld.COMMAND_NAME},
                      ${Database.HistoryOld.COMMAND_DATE},
                      ${Database.HistoryOld.JSON_DATA}
                 FROM ${Database.KEYSPACE_OLD}.${Database.HistoryOld.TABLE}
                WHERE ${Database.HistoryOld.COMMAND_ID}=?
                  AND ${Database.HistoryOld.COMMAND_NAME}=?
            """
    }

    private val preparedSaveHistoryCQL = session.prepare(SAVE_HISTORY_CQL)
    private val preparedFindHistoryByCpidAndCommandCQL = session.prepare(FIND_HISTORY_ENTRY_CQL)

    override fun getHistory(commandId: CommandId, action: Action): Result<String?, Fail.Incident.Database> =
        preparedFindHistoryByCpidAndCommandCQL.bind()
            .apply {
                setString(Database.HistoryOld.COMMAND_ID, commandId.underlying)
                setString(Database.HistoryOld.COMMAND_NAME, action.key)
            }
            .tryExecute(session)
            .onFailure { return it }
            .one()
            ?.getString(Database.HistoryOld.JSON_DATA)
            .asSuccess()

    override fun saveHistory(
        commandId: CommandId,
        action: Action,
        data: String
    ): Result<Boolean, Fail.Incident.Database> = preparedSaveHistoryCQL.bind()
        .apply {
            setString(Database.HistoryOld.COMMAND_ID, commandId.underlying)
            setString(Database.HistoryOld.COMMAND_NAME, action.key)
            setTimestamp(Database.HistoryOld.COMMAND_DATE, nowDefaultUTC().toCassandraTimestamp())
            setString(Database.HistoryOld.JSON_DATA, data)
        }
        .tryExecute(session)
        .onFailure { return it }
        .wasApplied()
        .asSuccess()
}
