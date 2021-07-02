package com.procurement.access.infrastructure.repository

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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Service
class CassandraTenderProcessRepositoryV2(@Qualifier("access") private val session: Session) : TenderProcessRepository {

    companion object {
        private const val GET_BY_CPID_AND_OCID_CQL = """
               SELECT ${Database.Tender.CPID},
                      ${Database.Tender.OCID},
                      ${Database.Tender.TOKEN},
                      ${Database.Tender.OWNER},                     
                      ${Database.Tender.CREATION_DATE},
                      ${Database.Tender.JSON_DATA}
                 FROM ${Database.KEYSPACE_ACCESS}.${Database.Tender.TABLE}
                WHERE ${Database.Tender.CPID}=?
                  AND ${Database.Tender.OCID}=?
            """
        private const val SAVE_CQL = """
          INSERT INTO ${Database.KEYSPACE_ACCESS}.${Database.Tender.TABLE}(
                      ${Database.Tender.CPID},
                      ${Database.Tender.OCID},
                      ${Database.Tender.TOKEN},
                      ${Database.Tender.OWNER},          
                      ${Database.Tender.CREATION_DATE},
                      ${Database.Tender.JSON_DATA}
          ) 
                VALUES(?, ?, ?, ?, ?, ?)
            """

        private const val UPDATE_CQL = """
               UPDATE ${Database.KEYSPACE_ACCESS}.${Database.Tender.TABLE}
                  SET ${Database.Tender.JSON_DATA}=?
                WHERE ${Database.Tender.CPID}=?
                  AND ${Database.Tender.OCID}=?
                  AND ${Database.Tender.TOKEN}=?
                IF EXISTS
            """
    }

    private val preparedGetByCpIdAndOcidCQL = session.prepare(GET_BY_CPID_AND_OCID_CQL)
    private val preparedSaveCQL = session.prepare(SAVE_CQL)
    private val updateCQL = session.prepare(UPDATE_CQL)

    override fun update(entity: TenderProcessEntity): Result<Boolean, Fail.Incident> =
        updateCQL.bind()
            .apply {
                setString(Database.Tender.CPID, entity.cpId.value)
                setString(Database.Tender.OCID, entity.ocid.value)
                setString(Database.Tender.TOKEN, entity.token.toString())
                setString(Database.Tender.JSON_DATA, entity.jsonData)
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
                setString(Database.Tender.CPID, entity.cpId.value)
                setString(Database.Tender.OCID, entity.ocid.value)
                setString(Database.Tender.TOKEN, entity.token.toString())
                setString(Database.Tender.OWNER, entity.owner)
                setTimestamp(Database.Tender.CREATION_DATE, entity.createdDate.toCassandraTimestamp())
                setString(Database.Tender.JSON_DATA, entity.jsonData)
            }
            .tryExecute(session)
            .onFailure { return it }
            .wasApplied()
            .asSuccess()

    override fun getByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity?, Fail.Incident.Database> =
        preparedGetByCpIdAndOcidCQL.bind()
            .apply {
                setString(Database.Tender.CPID, cpid.value)
                setString(Database.Tender.OCID, ocid.value)
            }
            .tryExecute(session)
            .onFailure { return it }
            .one()
            ?.convertToTenderProcessEntity()
            .asSuccess()

    private fun Row.convertToTenderProcessEntity(): TenderProcessEntity {
        return TenderProcessEntity(
            Cpid.tryCreateOrNull(this.getString(Database.Tender.CPID))!!,
            UUID.fromString(getString(Database.Tender.TOKEN)),
            getString(Database.Tender.OWNER),
            Ocid.SingleStage.tryCreateOrNull(this.getString(Database.Tender.OCID))!!,
            getTimestamp(Database.Tender.CREATION_DATE).toLocalDateTime(),
            getString(Database.Tender.JSON_DATA)
        )
    }
}