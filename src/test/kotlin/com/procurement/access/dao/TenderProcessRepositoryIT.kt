package com.procurement.access.dao

import com.datastax.driver.core.BatchStatement
import com.datastax.driver.core.BoundStatement
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.application.exception.repository.ReadEntityException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class TenderProcessRepositoryIT {
    companion object {
        private const val CPID = "cpid-1"
        private const val STAGE_PN = "PN"
        private const val STAGE_EV = "EV"
        private val TOKEN = UUID.randomUUID()
        private val OWNER = UUID.randomUUID().toString()
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var repository: TenderProcessDao

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

        repository = TenderProcessDao(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun findAuthByCpid() {
        insertAuths()

        val actualFundedAwardPeriodStartDate = repository.findAuthByCpid(cpid = CPID)

        assertEquals(2, actualFundedAwardPeriodStartDate.size)
    }

    @Test
    fun findAuthByCpidNotFound() {
        val actualFundedAwardPeriodStartDate = repository.findAuthByCpid(cpid = CPID)

        assertTrue(actualFundedAwardPeriodStartDate.isEmpty())
    }

    @Test
    fun errorRead() {

        doThrow(RuntimeException())
            .whenever(session)
            .execute(any<BoundStatement>())

        val exception = assertThrows<ReadEntityException> {
            repository.findAuthByCpid(cpid = CPID)
        }
        assertEquals("Error read auth data from the database.", exception.message)
    }

    private fun createKeyspace() {
        session.execute("CREATE KEYSPACE ocds WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};")
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE ocds;")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS ocds.access_tender (
                    cp_id text,
                    stage text,
                    token_entity UUID,
                    owner text,
                    created_date timestamp,
                    json_data text,
                    PRIMARY KEY(cp_id, stage, token_entity)
                );
            """
        )
    }

    private fun insertAuths() {
        val recPN = QueryBuilder.insertInto("ocds", "access_tender")
            .value("cp_id", CPID)
            .value("stage", STAGE_PN)
            .value("token_entity", TOKEN)
            .value("owner", OWNER)

        val recEV = QueryBuilder.insertInto("ocds", "access_tender")
            .value("cp_id", CPID)
            .value("stage", STAGE_EV)
            .value("token_entity", TOKEN)
            .value("owner", OWNER)

        val statement = BatchStatement().add(recPN).add(recEV)

        session.execute(statement)
    }
}
