package com.procurement.access.domain.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procurement.access.infrastructure.bind.jackson.configuration
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

internal class CpidTest {

    private val mapper: ObjectMapper = jacksonObjectMapper().apply { configuration() }

    companion object {
        private const val payload = """ { "cpid": "ocds-b3wdp1-MD-1580458690892" } """
    }

    @Test
    fun `success parsed cpid`() {
        assertDoesNotThrow { mapper.readValue(payload, SampleDTO::class.java) }
    }

    private data class SampleDTO(val cpid: Cpid)
}