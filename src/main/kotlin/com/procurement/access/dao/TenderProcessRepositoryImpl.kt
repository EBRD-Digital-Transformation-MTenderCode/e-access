package com.procurement.access.dao

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.procurement.access.application.repository.Auth
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.incident.DatabaseIncident
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.model.entity.TenderProcessEntity
import org.springframework.stereotype.Service

@Service
class TenderProcessRepositoryImpl(private val session: Session) : TenderProcessRepository {

    companion object {
        private const val KEY_SPACE = "ocds"
        private const val TABLE_NAME = "access_tender"
        private const val COLUMN_CPID = "cp_id"
        private const val COLUMN_TOKEN = "token_entity"
        private const val COLUMN_STAGE = "stage"
        private const val COLUMN_CREATION_DATE = "created_date"
        private const val COLUMN_OWNER = "owner"
        private const val COLUMN_JSON_DATA = "json_data"

        private const val FIND_AUTH_BY_CPID_CQL = """
               SELECT $COLUMN_TOKEN,
                      $COLUMN_OWNER
                 FROM $KEY_SPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=?
            """

        private const val GET_BY_CPID_AND_STAGE_CQL = """
               SELECT *
                 FROM $KEY_SPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=?
                  AND $COLUMN_STAGE=?
            """
        private const val SAVE_CQL = """
          INSERT INTO $KEY_SPACE.$TABLE_NAME(
          $COLUMN_CPID,
          $COLUMN_TOKEN,
          $COLUMN_OWNER,
          $COLUMN_STAGE,
          $COLUMN_CREATION_DATE,
          $COLUMN_JSON_DATA
          ) 
          VALUES(?, ?, ?, ?, ?, ?)
            """
    }

    private val preparedFindAuthByCpidCQL = session.prepare(FIND_AUTH_BY_CPID_CQL)
    private val preparedGetByCpIdAndStageCQL = session.prepare(GET_BY_CPID_AND_STAGE_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_CQL)

    override fun save(entity: TenderProcessEntity): Result<ResultSet, DatabaseIncident> {
        val insert = preparedSaveCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpId)
                setUUID(COLUMN_TOKEN, entity.token)
                setString(COLUMN_OWNER, entity.owner)
                setString(COLUMN_STAGE, entity.stage)
                QueryBuilder.set(COLUMN_CREATION_DATE, entity.createdDate)
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }
        return load(insert)
            .doOnError { error -> return failure(error) }
    }

    override fun getByCpIdAndStage(cpId: String, stage: String): Result<TenderProcessEntity?, DatabaseIncident> {
        val query = preparedGetByCpIdAndStageCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpId)
                setString(COLUMN_STAGE, stage)
            }

        return load(query)
            .doOnError { error -> return failure(error) }
            .get
            .one()
            ?.convertToTenderProcessEntity()
            .asSuccess()
    }

    override fun findAuthByCpid(cpid: String): Result<List<Auth>, DatabaseIncident> {
        val query = preparedFindAuthByCpidCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid)
            }

        return load(query)
            .doOnError { error -> return failure(error) }
            .get
            .map { row -> row.convertToContractEntity() }
            .asSuccess()
    }

    protected fun load(statement: BoundStatement): Result<ResultSet, DatabaseIncident> = try {
        success(session.execute(statement))
    } catch (expected: Exception) {
        failure(DatabaseIncident.Database(expected))
    }

    private fun Row.convertToTenderProcessEntity(): TenderProcessEntity {
        return TenderProcessEntity(
            this.getString(COLUMN_CPID),
            this.getUUID(COLUMN_TOKEN),
            this.getString(COLUMN_OWNER),
            this.getString(COLUMN_STAGE),
            this.getTimestamp(COLUMN_CREATION_DATE),
            this.getString(COLUMN_JSON_DATA)
        )
    }

    private fun Row.convertToContractEntity(): Auth = Auth(
        token = this.getUUID(COLUMN_TOKEN),
        owner = this.getString(COLUMN_OWNER)
    )
}