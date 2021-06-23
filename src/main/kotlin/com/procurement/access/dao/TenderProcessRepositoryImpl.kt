package com.procurement.access.dao

import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.infrastructure.extension.cassandra.toCassandraTimestamp
import com.procurement.access.infrastructure.extension.cassandra.toLocalDateTime
import com.procurement.access.infrastructure.extension.cassandra.tryExecute
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import com.procurement.access.lib.functional.Result.Companion.success
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.flatMap
import com.procurement.access.model.entity.TenderProcessEntity
import org.springframework.stereotype.Service

@Service
class TenderProcessRepositoryImpl(private val session: Session) : TenderProcessRepository {

    companion object {
        private const val KEY_SPACE = "ocds"
        private const val TABLE_NAME = "access_tender"
        private const val COLUMN_CPID = "cpid"
        private const val COLUMN_OCID = "ocid"
        private const val COLUMN_TOKEN = "token_entity"
        private const val COLUMN_CREATION_DATE = "created_date"
        private const val COLUMN_OWNER = "owner"
        private const val COLUMN_JSON_DATA = "json_data"

        private const val GET_BY_CPID_AND_OCID_CQL = """
               SELECT $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_TOKEN,
                      $COLUMN_OWNER,                     
                      $COLUMN_CREATION_DATE,
                      $COLUMN_JSON_DATA
                 FROM $KEY_SPACE.$TABLE_NAME
                WHERE $COLUMN_CPID=?
                  AND $COLUMN_OCID=?
            """
        private const val SAVE_CQL = """
          INSERT INTO $KEY_SPACE.$TABLE_NAME(
                      $COLUMN_CPID,
                      $COLUMN_OCID,
                      $COLUMN_TOKEN,
                      $COLUMN_OWNER,          
                      $COLUMN_CREATION_DATE,
                      $COLUMN_JSON_DATA
          ) 
                VALUES(?, ?, ?, ?, ?, ?)
            """

        private const val UPDATE_CQL = """
               UPDATE $KEY_SPACE.$TABLE_NAME
                  SET $COLUMN_JSON_DATA=?
                WHERE $COLUMN_CPID=?
                  AND $COLUMN_OCID=?
                  AND $COLUMN_TOKEN=?
                IF EXISTS
            """
    }

    private val preparedGetByCpIdAndOcidCQL = session.prepare(GET_BY_CPID_AND_OCID_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_CQL)
    private val updateCQL = session.prepare(UPDATE_CQL)

    override fun update(entity: TenderProcessEntity): Result<Boolean, Fail.Incident> =
        updateCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpId.value)
                setString(COLUMN_OCID, entity.ocid.value)
                setUUID(COLUMN_TOKEN, entity.token)
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }
            .tryExecute(session)
            .flatMap {
                if (!it.wasApplied()) {
                    val mdc = mapOf(
                        "description" to "Cannot update record",
                        "cpid" to entity.cpId.value,
                        "ocid" to entity.ocid.value,
                        "data" to entity.jsonData
                    )
                    failure(Fail.Incident.DatabaseIncident(mdc = mdc))
                } else
                    success(it.wasApplied())
            }

    override fun save(entity: TenderProcessEntity): Result<Boolean, Fail.Incident.Database> =
        preparedSaveCQL.bind()
            .apply {
                setString(COLUMN_CPID, entity.cpId.value)
                setString(COLUMN_OCID, entity.ocid.value)
                setUUID(COLUMN_TOKEN, entity.token)
                setString(COLUMN_OWNER, entity.owner)
                setTimestamp(COLUMN_CREATION_DATE, entity.createdDate.toCassandraTimestamp())
                setString(COLUMN_JSON_DATA, entity.jsonData)
            }
            .tryExecute(session)
            .onFailure { return it }
            .wasApplied()
            .asSuccess()

    override fun getByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity?, Fail.Incident.Database> =
        preparedGetByCpIdAndOcidCQL.bind()
            .apply {
                setString(COLUMN_CPID, cpid.value)
                setString(COLUMN_OCID, ocid.value)
            }
            .tryExecute(session)
            .onFailure { return it }
            .one()
            ?.convertToTenderProcessEntity()
            .asSuccess()

    private fun Row.convertToTenderProcessEntity(): TenderProcessEntity {
        return TenderProcessEntity(
            Cpid.tryCreateOrNull(this.getString(COLUMN_CPID))!!,
            this.getUUID(COLUMN_TOKEN),
            this.getString(COLUMN_OWNER),
            Ocid.SingleStage.tryCreateOrNull(this.getString(COLUMN_OCID))!!,
            this.getTimestamp(COLUMN_CREATION_DATE).toLocalDateTime(),
            this.getString(COLUMN_JSON_DATA)
        )
    }
}