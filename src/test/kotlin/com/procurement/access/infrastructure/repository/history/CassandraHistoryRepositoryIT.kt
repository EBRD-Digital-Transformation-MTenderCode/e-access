package com.procurement.access.infrastructure.repository.history

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.nhaarman.mockito_kotlin.spy
import com.procurement.access.dao.CassandraTestContainer
import com.procurement.access.dao.DatabaseTestConfiguration
import com.procurement.access.infrastructure.api.Action
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.infrastructure.api.v1.CommandTypeV1
import com.procurement.access.infrastructure.handler.HistoryRepository
import com.procurement.access.infrastructure.repository.CassandraHistoryRepository
import com.procurement.access.infrastructure.repository.Database
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class CassandraHistoryRepositoryIT {

    companion object {
        private val COMMAND_ID: CommandId = CommandId(UUID.randomUUID().toString())
        private val ACTION: Action = CommandTypeV1.AMEND_FE
        private const val JSON_DATA: String = """{"tender": {"title" : "Tender-Title"}}"""
    }

    @Autowired
    private lateinit var container: CassandraTestContainer
    private lateinit var session: Session
    private lateinit var repository: HistoryRepository

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

        repository = CassandraHistoryRepository(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()
    }

    @Test
    fun saveHistory() {
        val result = repository.saveHistory(commandId = COMMAND_ID, action = ACTION, data = JSON_DATA)

        assertTrue(result.isSuccess)
        result.forEach {
            assertTrue(it)
        }
    }

    @Test
    fun getHistory() {

        val savedResult = repository.saveHistory(commandId = COMMAND_ID, action = ACTION, data = JSON_DATA)

        assertTrue(savedResult.isSuccess)
        savedResult.forEach {
            assertTrue(it)
        }

        val loadedResult = repository.getHistory(commandId = COMMAND_ID, action = ACTION)
        assertTrue(loadedResult.isSuccess)
        loadedResult.forEach {
            assertNotNull(it)
            assertEquals(JSON_DATA, it)
        }
    }

    private fun createKeyspace() {
        session.execute(
            "CREATE KEYSPACE ${Database.KEYSPACE} " +
                "WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};"
        )
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE ${Database.KEYSPACE};")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS ${Database.KEYSPACE}.${Database.History.TABLE}
                    (
                        ${Database.History.COMMAND_ID}   TEXT,
                        ${Database.History.COMMAND_NAME} TEXT,
                        ${Database.History.COMMAND_DATE} TIMESTAMP,
                        ${Database.History.JSON_DATA}    TEXT,
                        PRIMARY KEY (${Database.History.COMMAND_ID}, ${Database.History.COMMAND_NAME})
                    );
            """
        )
    }
}
