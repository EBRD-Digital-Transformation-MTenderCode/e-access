package com.procurement.access.infrastructure.dto.cn

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonFilePathGenerator
import com.procurement.access.model.dto.ocds.ProcurementMethod
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class CnOnPnRequestTest : AbstractDTOTestBase<CnOnPnRequest>(CnOnPnRequest::class.java) {

    @Nested
    inner class OP {
        private val pmd = ProcurementMethod.OT

        @Nested
        inner class WithAuctions {
            private val hasAuctions = true
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToFullyJsonFile(
                    pmd = pmd,
                    hasAuctions = hasAuctions,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Nested
        inner class WithoutAuctions {
            private val hasAuctions = false
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToFullyJsonFile(
                    pmd = pmd,
                    hasAuctions = hasAuctions,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Test
        fun onlyRequiredAttributes() {
            val pathToJsonFile = pathToRequiredJsonFile(pmd)
            testBindingAndMapping(pathToJsonFile)
        }
    }

    @Nested
    inner class LP {
        private val pmd = ProcurementMethod.DA

        @Nested
        inner class WithoutAuctions {
            private val hasAuctions = false
            private val hasDocuments = true

            @Test
            fun test() {
                val pathToJsonFile = pathToFullyJsonFile(
                    pmd = pmd,
                    hasAuctions = hasAuctions,
                    hasDocuments = hasDocuments
                )
                testBindingAndMapping(pathToJsonFile)
            }
        }

        @Test
        fun onlyRequiredAttributes() {
            val pathToJsonFile = pathToRequiredJsonFile(pmd)
            testBindingAndMapping(pathToJsonFile)
        }
    }

    private fun pathToFullyJsonFile(
        pmd: ProcurementMethod,
        hasAuctions: Boolean,
        hasDocuments: Boolean,
        isFully: Boolean = true
    ) = JsonFilePathGenerator.CnOnPn.request(
        pmd = pmd,
        hasAuctions = hasAuctions,
        hasDocuments = hasDocuments,
        isFully = isFully
    )

    private fun pathToRequiredJsonFile(pmd: ProcurementMethod): String {
        val prefix = JsonFilePathGenerator.CnOnPn.PATH_PREFIX_REQUEST
        val pmdSegment = JsonFilePathGenerator.pmdSegment(pmd = pmd)
        val requiredSegment = JsonFilePathGenerator.requiredSegment(false)
        return "$prefix/$pmdSegment/request_cn_on_pn_${requiredSegment}.json"
    }
}
