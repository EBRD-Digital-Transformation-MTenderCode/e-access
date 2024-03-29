package com.procurement.access.domain.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procurement.access.infrastructure.bind.jackson.configuration
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OcidTest {

    private val mapper: ObjectMapper = jacksonObjectMapper().apply { configuration() }

    companion object {
        private const val payload = """ { "ocid": "ocds-b3wdp1-MD-1580458690892-EV-1580458791896" } """
    }

    @Test
    fun `success parsed ocid`() {
        assertDoesNotThrow { mapper.readValue(payload, SampleDTO::class.java) }
    }

    private data class SampleDTO(val ocid: Ocid.SingleStage)

    @Test
    fun extractCpidOrNull_singleStage_success(){
        val cpidString = "ocds-b3wdp1-MD-1580458690892"
        val ocid = Ocid.SingleStage.tryCreateOrNull("$cpidString-EV-1580458791896")!!

        val actual = ocid.extractCpid()
        val expected = Cpid.tryCreateOrNull(cpidString)!!

        assertEquals(expected, actual)
    }

    @Test
    fun extractCpidOrNull_multiStage_success1(){
        val cpidString = "ocds-b3wdp1-MD-1580458690892"
        val ocid = Ocid.MultiStage.tryCreateOrNull(cpidString)!!

        val actual = ocid.extractCpid()
        val expected = Cpid.tryCreateOrNull(cpidString)!!

        assertEquals(expected, actual)
    }
}