package com.procurement.access.dao

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.HostDistance
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.PoolingOptions
import com.datastax.driver.core.Session
import com.nhaarman.mockito_kotlin.spy
import com.procurement.access.application.repository.TenderProcessRepository
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.model.entity.TenderProcessEntity
import com.procurement.access.utils.toDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [DatabaseTestConfiguration::class])
class TenderProcessRepositoryIT {

    companion object {
        private val CPID = Cpid.tryCreateOrNull("ocds-t1s2t3-MD-1565251033096")!!
        private val STAGE = Stage.PN
        private val TOKEN = UUID.randomUUID()
        private val DATE = LocalDateTime.now().toDate()

        private val RANDOM_UUID = UUID.randomUUID()

        private const val KEYSPACE = "ocds"
    }

    @Autowired
    private lateinit var container: CassandraTestContainer

    private lateinit var session: Session
    private lateinit var cassandraCluster: Cluster

    private lateinit var tenderProccessRepository: TenderProcessRepository

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

        cassandraCluster = cluster
        session = spy(cluster.connect())

        createKeyspace()
        createTable()

        tenderProccessRepository = TenderProcessRepositoryImpl(session)
    }

    @AfterEach
    fun clean() {
        dropKeyspace()

        session.close()
        cassandraCluster.closeAsync()
    }

    @Test
    fun update_record_success() {
        val dataForUpdate = """ { "${RANDOM_UUID}": "${RANDOM_UUID}" } """
        val entity = SAMPLE_ENTITY
        tenderProccessRepository.save(entity)

        val updatedEntity = entity.copy(jsonData = dataForUpdate)
        val wasApplied = tenderProccessRepository.update(updatedEntity).get
        assertTrue(wasApplied)

        val storedEntity = tenderProccessRepository.getByCpIdAndStage(
            Cpid.tryCreateOrNull(entity.cpId)!!,
            Stage.creator(entity.stage)
        ).get!!

        assertEquals(dataForUpdate, storedEntity.jsonData)

    }

    val SAMPLE_ENTITY = TenderProcessEntity(
        cpId = CPID.toString(),
        stage = STAGE.toString(),
        owner = "sample-owner",
        token = TOKEN,
        createdDate = DATE,
        jsonData = "{  }"
    )


    private fun createKeyspace() {
        session.execute(
            "CREATE KEYSPACE $KEYSPACE " +
                "WITH replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1};"
        )
    }

    private fun dropKeyspace() {
        session.execute("DROP KEYSPACE $KEYSPACE;")
    }

    private fun createTable() {
        session.execute(
            """
                CREATE TABLE IF NOT EXISTS ${KEYSPACE}.access_tender (
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


}


