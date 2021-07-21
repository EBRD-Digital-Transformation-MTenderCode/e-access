package com.procurement.access.infrastructure.repository

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.spy
import com.procurement.access.application.repository.CriteriaRepository
import com.procurement.access.dao.CassandraTestContainer
import com.procurement.access.dao.DatabaseTestConfiguration
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
class CassandraCriteriaRepositoryIT {

    companion object {

        private const val COUNTRY = "MD"
        private const val LANGUAGE = "EN"
        private const val JSON_DATE = "VAL"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer
    private lateinit var session: Session
    private lateinit var repository: CriteriaRepository

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

        repository = CassandraCriteriaRepository(session = session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Nested
    inner class Find {
        @Test
        fun findValue() {
            insertCriteria()

            val result = repository.find(country = COUNTRY, language = LANGUAGE)

            assertTrue(result.isSuccess)
            result.forEach {
                assertEquals(it, JSON_DATE)
            }
        }

        @Test
        fun notFoundValue() {
            val result = repository.find(country = COUNTRY, language = LANGUAGE)

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
                CREATE TABLE IF NOT EXISTS ${Database.KEYSPACE_ACCESS}.${Database.Criteria.TABLE}
                    (
                        ${Database.Criteria.COUNTRY}    TEXT,
                        ${Database.Criteria.LANGUAGE}   TEXT,
                        ${Database.Criteria.JSON_DATA}  TEXT,
                        PRIMARY KEY (${Database.Criteria.COUNTRY}, ${Database.Criteria.LANGUAGE})
                    );
            """
        )
    }

    fun insertCriteria() {
        val record = QueryBuilder.insertInto(Database.KEYSPACE_ACCESS, Database.Criteria.TABLE)
            .value(Database.Criteria.COUNTRY, COUNTRY)
            .value(Database.Criteria.LANGUAGE, LANGUAGE)
            .value(Database.Criteria.JSON_DATA, JSON_DATE)

        session.execute(record)
    }
}
