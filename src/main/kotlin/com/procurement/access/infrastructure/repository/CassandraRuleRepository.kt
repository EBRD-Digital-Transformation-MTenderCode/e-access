package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.Session
import com.procurement.access.application.repository.RuleRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.infrastructure.extension.cassandra.tryExecute
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Repository

@Repository
class CassandraRuleRepository(@Qualifier("ocds") private val session: Session) : RuleRepository {

    companion object {

        private const val GET_RULE_CQL = """
            SELECT ${Database.Rules.VALUE}
              FROM ${Database.KEYSPACE_ACCESS}.${Database.Rules.TABLE}
             WHERE ${Database.Rules.COUNTRY}=?
               AND ${Database.Rules.PMD}=?
               AND ${Database.Rules.OPERATION_TYPE}=?
               AND ${Database.Rules.PARAMETER}=?
        """
    }

    private val preparedGetRule = session.prepare(GET_RULE_CQL)

    override fun find(
        country: String,
        pmd: ProcurementMethod,
        operationType: String,
        parameter: String
    ): Result<String?, Fail.Incident.Database> =
        preparedGetRule.bind()
            .apply {
                setString(Database.Rules.COUNTRY, country)
                setString(Database.Rules.PMD, pmd.name)
                setString(Database.Rules.OPERATION_TYPE, operationType)
                setString(Database.Rules.PARAMETER, parameter)
            }
            .tryExecute(session)
            .onFailure { return it }
            .one()
            ?.getString(Database.Rules.VALUE)
            .asSuccess()
}
