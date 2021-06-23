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
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
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
class TenderProcessDaoIT {
    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1543525135421")!!
        private val OCID = Ocid.SingleStage.tryCreateOrNull("ocds-b3wdp1-MD-1580458690892-EV-1580458791896")
        private const val OCID_1 = "ocds-b3wdp1-MD-1581509539187-EV-1581509653044"
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
                    cpid text,
                    ocid text,
                    token_entity UUID,
                    owner text,
                    created_date timestamp,
                    json_data text,
                    PRIMARY KEY(cpid, ocid, token_entity)
                );
            """
        )
    }

    private fun insertAuths() {
        val recPN = QueryBuilder.insertInto("ocds", "access_tender")
            .value("cpid", CPID)
            .value("ocid", OCID)
            .value("token_entity", TOKEN)
            .value("owner", OWNER)

        val recEV = QueryBuilder.insertInto("ocds", "access_tender")
            .value("cpid", CPID)
            .value("ocid", OCID_1)
            .value("token_entity", TOKEN)
            .value("owner", OWNER)

        val statement = BatchStatement().add(recPN).add(recEV)

        session.execute(statement)
    }
}
