package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder.eq
import com.datastax.driver.core.querybuilder.QueryBuilder.insertInto
import com.datastax.driver.core.querybuilder.QueryBuilder.select
import com.procurement.access.application.exception.repository.ReadEntityException
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.infrastructure.extension.cassandra.toCassandraTimestamp
import com.procurement.access.infrastructure.extension.cassandra.toLocalDateTime
import com.procurement.access.model.entity.TenderProcessEntity
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import java.util.*

@Service
class CassandraTenderProcessRepositoryV1(@Qualifier("access") private val session: Session) {

    companion object {

        private const val FIND_AUTH_BY_CPID_CQL = """
               SELECT ${Database.Tender.TOKEN},
                      ${Database.Tender.OWNER}
                 FROM ${Database.KEYSPACE_ACCESS}.${Database.Tender.TABLE}
                WHERE ${Database.Tender.CPID}=?
            """
    }

    private val preparedFindAuthByCpidCQL = session.prepare(FIND_AUTH_BY_CPID_CQL)

    fun save(entity: TenderProcessEntity) {
        val insert = insertInto(Database.Tender.TABLE)
        insert.value(Database.Tender.CPID, entity.cpId.value)
            .value(Database.Tender.TOKEN, entity.token.toString())
            .value(Database.Tender.OWNER, entity.owner)
            .value(Database.Tender.OCID, entity.ocid.value)
            .value(Database.Tender.CREATION_DATE, entity.createdDate.toCassandraTimestamp())
            .value(Database.Tender.JSON_DATA, entity.jsonData)
        session.execute(insert)
    }

    fun getByCpidAndOcid(cpid: Cpid, ocid: Ocid): TenderProcessEntity? {
        val query = select()
            .all()
            .from(Database.Tender.TABLE)
            .where(eq(Database.Tender.CPID, cpid.value))
            .and(eq(Database.Tender.OCID, ocid.value)).limit(1)
        return session.execute(query)
            .one()
            ?.let { row ->
                TenderProcessEntity(
                    Cpid.tryCreateOrNull(row.getString(Database.Tender.CPID))!!,
                    UUID.fromString(row.getString(Database.Tender.TOKEN)),
                    row.getString(Database.Tender.OWNER),
                    Ocid.SingleStage.tryCreateOrNull(row.getString(Database.Tender.OCID))!!,
                    row.getTimestamp(Database.Tender.CREATION_DATE).toLocalDateTime(),
                    row.getString(Database.Tender.JSON_DATA)
                )
            }
    }

    fun findAuthByCpid(cpid: Cpid): List<Auth> {
        val query = preparedFindAuthByCpidCQL.bind()
            .apply {
                setString(Database.Tender.CPID, cpid.value)
            }

        val resultSet = load(query)
        return resultSet.map { convertToContractEntity(it) }
    }

    protected fun load(statement: BoundStatement): ResultSet = try {
        session.execute(statement)
    } catch (exception: Exception) {
        throw ReadEntityException(message = "Error read auth data from the database.", cause = exception)
    }

    private fun convertToContractEntity(row: Row): Auth = Auth(
        token = UUID.fromString(row.getString(Database.Tender.TOKEN)),
        owner = row.getString(Database.Tender.OWNER)
    )

    class Auth(val token: UUID, val owner: String)
}
