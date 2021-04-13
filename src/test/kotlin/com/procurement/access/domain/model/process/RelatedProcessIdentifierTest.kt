package com.procurement.access.domain.model.process

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.procurement.access.domain.model.Cpid
import com.procurement.access.infrastructure.bind.jackson.configuration
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RelatedProcessIdentifierTest {

    private val mapper: ObjectMapper = jacksonObjectMapper().apply { configuration() }

    companion object {
        private const val CPID = "ocds-b3wdp1-MD-1580458690892"
        private const val OCID = "ocds-b3wdp1-MD-1580458690892-EV-1580458791896"
        private const val cpidPayload = """ { "identifier": "$CPID" } """
        private const val ocidPayload = """ { "identifier": "$OCID" } """
    }

    @Test
    fun `success parsed cpid`() {
        assertDoesNotThrow { mapper.readValue(cpidPayload, SampleDTO::class.java) }
    }

    @Test
    fun `success parsed ocid`() {
        assertDoesNotThrow { mapper.readValue(ocidPayload, SampleDTO::class.java) }
    }

    @Test
    fun `the same objects are equals`() {
        val identifier1 = RelatedProcessIdentifier.of(Cpid.tryCreateOrNull(CPID)!!)
        val identifier2 = RelatedProcessIdentifier.of(Cpid.tryCreateOrNull(CPID)!!)
        assertEquals(identifier1, identifier2)
    }

    private data class SampleDTO(val identifier: RelatedProcessIdentifier)
}