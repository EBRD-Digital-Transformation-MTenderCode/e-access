package com.procurement.access.infrastructure.entity

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonFilePathGenerator
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PNEntityTest : AbstractDTOTestBase<PNEntity>(PNEntity::class.java) {

    @Nested
    inner class WithItems {
        private val hasItems = true

        @Nested
        inner class WithDocuments {
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(
                    hasItems = hasItems,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Nested
        inner class WithoutDocuments {
            private val hasDocuments = false

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(
                    hasItems = hasItems,
                    hasDocuments = hasDocuments
                )
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
                val pathToJsonFile = pathToJsonFile(
                    hasItems = hasItems,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Nested
        inner class WithoutDocuments {
            private val hasDocuments = false

            @Test
            fun test() {
                val pathToJsonFile = pathToJsonFile(
                    hasItems = hasItems,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }
    }

    private fun pathToJsonFile(hasItems: Boolean, hasDocuments: Boolean) =
        JsonFilePathGenerator.Entites.pn(
            hasItems = hasItems,
            hasDocuments = hasDocuments
        )
}
