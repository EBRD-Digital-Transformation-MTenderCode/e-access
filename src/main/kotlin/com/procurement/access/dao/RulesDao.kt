package com.procurement.access.dao

import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Row
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder.eq
import com.datastax.driver.core.querybuilder.QueryBuilder.select
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import org.springframework.stereotype.Service

@Service
class RulesDao(private val session: Session) {

    private val preparedGetDataCQL = session.prepare(GET_VALUE_CQL)

    fun getValue(country: String, pmd: ProcurementMethod, operationType: String, parameter: String): String? {
        val query = select()
            .column(COLUMN_VALUE)
            .from(RULES_TABLE)
            .where(eq(COLUMN_COUNTRY, country))
            .and(eq(COLUMN_PMD, pmd.name))
            .and(eq(COLUMN_OPERATION_TYPE, operationType))
            .and(eq(COLUMN_PARAMETER, parameter))
            .limit(1)
        val row = session.execute(query).one()
        return row?.getString(COLUMN_VALUE)
    }

    fun getData(country: String, pmd: ProcurementMethod, operationType: OperationType, parameter: String):Result<String?, Fail.Incident>{
        val statement = preparedGetDataCQL.bind()
            .apply {
                this.setString(COLUMN_COUNTRY, country)
                this.setString(COLUMN_PMD, pmd.name)
                this.setString(COLUMN_OPERATION_TYPE, operationType.key)
                this.setString(COLUMN_PARAMETER, parameter)
            }

        return load(statement)
            .orForwardFail { fail-> return fail }
            .one()
            ?.convert()
            .asSuccess()
    }

    private fun Row.convert(): String = this.getString(COLUMN_VALUE)

    protected fun load(statement: BoundStatement): Result<ResultSet, Fail.Incident.Database> = try {
        Result.success(session.execute(statement))
    } catch (expected: Exception) {
        Result.failure(Fail.Incident.Database(expected))
    }

    companion object {
        private const val KEYSPACE = "ocds"
        private const val RULES_TABLE = "access_rules"
        private const val COLUMN_COUNTRY = "country"
        private const val COLUMN_PMD = "pmd"
        private const val COLUMN_OPERATION_TYPE = "operation_type"
        private const val COLUMN_PARAMETER = "parameter"
        private const val COLUMN_VALUE = "value"

        private const val GET_VALUE_CQL = """
            SELECT $COLUMN_VALUE
              FROM $KEYSPACE.$RULES_TABLE
             WHERE $COLUMN_COUNTRY=?
               AND $COLUMN_PMD=?
               AND $COLUMN_OPERATION_TYPE=?
               AND $COLUMN_PARAMETER=?
        """
    }
}
