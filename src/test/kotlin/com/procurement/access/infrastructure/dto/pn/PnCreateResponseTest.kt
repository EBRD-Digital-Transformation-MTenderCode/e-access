package com.procurement.access.infrastructure.dto.pn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonFilePathGenerator
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PnCreateResponseTest : AbstractDTOTestBase<PnCreateResponse>(PnCreateResponse::class.java) {

    @Nested
    inner class WithItems {
        private val hasItems = true

        @Nested
        inner class WithDocuments {
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(hasItems = hasItems, hasDocuments = hasDocuments)
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Nested
        inner class WithoutDocuments {
            private val hasDocuments = false

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(hasItems = hasItems, hasDocuments = hasDocuments)
                testBindingAndMapping(pathToJsonFile)
            }
        }
    }

    @Nested
    inner class WithoutItems {
        private val hasItems = false

        @Nested
        inner class WithDocuments {
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(hasItems = hasItems, hasDocuments = hasDocuments)
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Nested
        inner class WithoutDocuments {
            private val hasDocuments = false

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(hasItems = hasItems, hasDocuments = hasDocuments)
                testBindingAndMapping(pathToJsonFile)
            }
        }
    }

    @Test
    fun required1() {
        val pathToJsonFile = "json/dto/create/pn/response/response_pn_required_1.json"
        testBindingAndMapping(pathToJsonFile)
    }

    @Test
    fun required2() {
        val pathToJsonFile = "json/dto/create/pn/response/response_pn_required_2.json"
        testBindingAndMapping(pathToJsonFile)
    }

    private fun pathToJsonFile(hasItems: Boolean, hasDocuments: Boolean) =
        JsonFilePathGenerator.Pn.response(hasItems = hasItems, hasDocuments = hasDocuments)
}
