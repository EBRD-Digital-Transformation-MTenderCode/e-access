package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonFilePathGenerator
import com.procurement.access.model.dto.ocds.ProcurementMethod
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CnOnPnResponseTest : AbstractDTOTestBase<CnOnPnResponse>(CnOnPnResponse::class.java) {

    @Nested
    inner class OP {
        private val pmd = ProcurementMethod.OT

        @Nested
        inner class WithAuctions {
            private val hasAuctions = true

            @Nested
            inner class WithItems {
                private val hasItems = true

                @Nested
                inner class WithDocuments {
                    private val hasDocuments = true

                    @Test
                    fun test() {
                        val pathToJsonFile = pathToJsonFile(
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
                            hasItems = hasItems,
                            hasDocuments = hasDocuments
                        )
                        testBindingAndMapping(pathToJsonFile)
                    }
                }
            }
        }

        @Nested
        inner class WithoutAuctions {
            private val hasAuctions = false

            @Nested
            inner class WithItems {
                private val hasItems = true

                @Nested
                inner class WithDocuments {
                    private val hasDocuments = true

                    @Test
                    fun test() {
                        val pathToJsonFile = pathToJsonFile(
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
                            hasItems = hasItems,
                            hasDocuments = hasDocuments
                        )
                        testBindingAndMapping(pathToJsonFile)
                    }
                }
            }
        }
    }

    @Nested
    inner class LP {
        private val pmd = ProcurementMethod.DA

        @Nested
        inner class WithoutAuctions {
            private val hasAuctions = false

            @Nested
            inner class WithItems {
                private val hasItems = true

                @Nested
                inner class WithDocuments {
                    private val hasDocuments = true

                    @Test
                    fun test() {
                        val pathToJsonFile = pathToJsonFile(
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
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
                            pmd = pmd,
                            hasAuctions = hasAuctions,
                            hasItems = hasItems,
                            hasDocuments = hasDocuments
                        )
                        testBindingAndMapping(pathToJsonFile)
                    }
                }
            }
        }
    }

    private fun pathToJsonFile(
        pmd: ProcurementMethod,
        hasAuctions: Boolean,
        hasItems: Boolean,
        hasDocuments: Boolean
    ) = JsonFilePathGenerator.CnOnPn.response(
        pmd = pmd,
        hasAuctions = hasAuctions,
        hasItems = hasItems,
        hasDocuments = hasDocuments
    )
}
