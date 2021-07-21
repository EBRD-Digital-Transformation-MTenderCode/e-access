package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.access.application.repository.CriteriaRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.extension.cassandra.tryExecute
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository

@Repository
class CassandraCriteriaRepository(@Qualifier("access") private val session: Session) : CriteriaRepository {

    companion object {

        private const val GET_CRITERIA_CQL = """
           SELECT ${Database.Criteria.JSON_DATA}
              FROM ${Database.KEYSPACE_ACCESS}.${Database.Criteria.TABLE}
             WHERE ${Database.Criteria.COUNTRY}=?
               AND ${Database.Criteria.LANGUAGE}=?
        """
    }

    private val preparedGetRule = session.prepare(GET_CRITERIA_CQL)

    override fun find(country: String, language: String): Result<String?, Fail.Incident.Database> =
        preparedGetRule.bind()
            .apply {
                setString(Database.Criteria.COUNTRY, country)
                setString(Database.Criteria.LANGUAGE, language)
            }
            .tryExecute(session)
            .onFailure { return it }
            .one()
            ?.getString(Database.Criteria.JSON_DATA)
            .asSuccess()
}
