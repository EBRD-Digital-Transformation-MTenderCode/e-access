package com.procurement.access.dao

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.domain.util.Result
import com.procurement.access.domain.util.Result.Companion.failure
import com.procurement.access.domain.util.Result.Companion.success
import com.procurement.access.domain.util.asSuccess
import com.procurement.access.domain.util.bind
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

        private const val GET_BY_CPID_AND_STAGE_CQL = """
               SELECT $COLUMN_CPID,
                      $COLUMN_TOKEN,
                      $COLUMN_OWNER,
                      $COLUMN_STAGE,
                      $COLUMN_CREATION_DATE,
                      $COLUMN_JSON_DATA
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

        private const val UPDATE_CQL = """
               UPDATE $KEY_SPACE.$TABLE_NAME
                  SET $COLUMN_JSON_DATA=?
                WHERE $COLUMN_CPID=?
                  AND $COLUMN_STAGE=?
                  AND $COLUMN_TOKEN=?
                IF EXISTS
            """
    }

    private val preparedGetByCpIdAndStageCQL = session.prepare(GET_BY_CPID_AND_STAGE_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_CQL)
    private val updateCQL = session.prepare(UPDATE_CQL)

    override fun update(entity: TenderProcessEntity): Result<Boolean, Fail.Incident>  {
        val updateStatement = updateCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpId)
                setString(COLUMN_STAGE, entity.stage)
                setUUID(COLUMN_TOKEN, entity.token)
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }

        return tryExecute(updateStatement)
            .map { it.wasApplied() }
            .bind { wasApplied ->
                if (!wasApplied) {
                    val mdc = mapOf(
                        "description" to "Cannot update record",
                        "cpid" to entity.cpId,
                        "stage" to entity.stage,
                        "data" to entity.jsonData
                    )
                    failure(Fail.Incident.DatabaseIncident(mdc = mdc))
                } else
                    success(wasApplied)
            }
    }

    override fun save(entity: TenderProcessEntity): Result<ResultSet, Fail.Incident.Database> {
        val insert = preparedSaveCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpId)
                setUUID(COLUMN_TOKEN, entity.token)
                setString(COLUMN_OWNER, entity.owner)
                setString(COLUMN_STAGE, entity.stage)
                setTimestamp(COLUMN_CREATION_DATE, entity.createdDate)
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }
        return tryExecute(insert)
            .doOnError { error -> return failure(error) }
    }

    override fun getByCpIdAndStage(cpid: Cpid, stage: Stage): Result<TenderProcessEntity?, Fail.Incident.Database> {
        val query = preparedGetByCpIdAndStageCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.toString())
                setString(COLUMN_STAGE, stage.toString())
            }

        return tryExecute(query)
            .doOnError { error -> return failure(error) }
            .get
            .one()
            ?.convertToTenderProcessEntity()
            .asSuccess()
    }

    protected fun tryExecute(statement: BoundStatement): Result<ResultSet, Fail.Incident.Database> = try {
        success(session.execute(statement))
    } catch (expected: Exception) {
        failure(Fail.Incident.Database(expected))
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
}