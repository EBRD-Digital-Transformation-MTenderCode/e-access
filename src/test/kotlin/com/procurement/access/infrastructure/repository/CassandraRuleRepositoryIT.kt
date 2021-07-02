package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.spy
import com.procurement.access.application.repository.RuleRepository
import com.procurement.access.dao.CassandraTestContainer
import com.procurement.access.dao.DatabaseTestConfiguration
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class CassandraRuleRepositoryIT {

    companion object {

        private const val COUNTRY = "MD"
        private val PMD = ProcurementMethod.GPA
        private val OPERATION_TYPE = OperationType.CREATE_PCR
        private const val PARAMETER = "PARAM"
        private const val VALUE = "VAL"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer
    private lateinit var session: Session
    private lateinit var repository: RuleRepository

    @BeforeEach
    fun init() {
        val poolingOptions = PoolingOptions()
            .setMaxConnectionsPerHost(HostDistance.LOCAL, 1)
        val cluster = Cluster.builder()
            .addContactPoints(container.contractPoint)
            .withPort(container.port)
            .withoutJMXReporting()
            .withPoolingOptions(poolingOptions)
            .withAuthProvider(PlainTextAuthProvider(container.username, container.password))
            .build()

        session = spy(cluster.connect())

        createKeyspace()
        createTable()

        repository = CassandraRuleRepository(session = session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Nested
    inner class Find {
        @Test
        fun findValue() {
            insertRule(parameter = PARAMETER)

            val result = repository.find(
                country = COUNTRY,
                pmd = PMD,
                operationType = OPERATION_TYPE,
                parameter = PARAMETER
            )

            assertTrue(result.isSuccess)
            result.forEach {
                assertEquals(it, VALUE)
            }
        }

        @Test
        fun notFoundValue() {
            val result = repository.find(
                country = COUNTRY,
                pmd = PMD,
                operationType = OPERATION_TYPE,
                parameter = PARAMETER
            )

            assertTrue(result.isSuccess)
            result.forEach {
                assertNull(it)
            }
        }
    }

    private fun createKeyspace() {
        session.execute(
            "CREATE KEYSPACE ${Database.KEYSPACE_ACCESS} " +
                "WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};"
        )
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE ${Database.KEYSPACE_ACCESS};")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS ${Database.KEYSPACE_ACCESS}.${Database.Rules.TABLE}
                    (
                        ${Database.Rules.COUNTRY}        TEXT,
                        ${Database.Rules.PMD}            TEXT,
                        ${Database.Rules.OPERATION_TYPE} TEXT,
                        ${Database.Rules.PARAMETER}      TEXT,
                        ${Database.Rules.VALUE}          TEXT,
                        PRIMARY KEY (${Database.Rules.COUNTRY}, ${Database.Rules.PMD}, ${Database.Rules.OPERATION_TYPE}, ${Database.Rules.PARAMETER})
                    );
            """
        )
    }

    fun insertRule(
        country: String = COUNTRY,
        pmd: ProcurementMethod = PMD,
        operationType: OperationType = OPERATION_TYPE,
        parameter: String,
        value: String = VALUE
    ) {
        val record = QueryBuilder.insertInto(Database.KEYSPACE_ACCESS, Database.Rules.TABLE)
            .value(Database.Rules.COUNTRY, country)
            .value(Database.Rules.PMD, pmd.name)
            .value(Database.Rules.OPERATION_TYPE, operationType.key)
            .value(Database.Rules.PARAMETER, parameter)
            .value(Database.Rules.VALUE, value)

        session.execute(record)
    }
}
