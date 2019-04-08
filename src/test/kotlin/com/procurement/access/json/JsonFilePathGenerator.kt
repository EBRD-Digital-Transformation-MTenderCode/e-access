package com.procurement.access.json

import com.procurement.access.model.dto.ocds.ProcurementMethod

object JsonFilePathGenerator {

    object Entites {
        val PATH_PREFIX_PN = "json/entity/pn"
        val PATH_PREFIX_CN = "json/entity/cn"

        fun pn(hasItems: Boolean, hasDocuments: Boolean, isFully: Boolean = true): String {
            val itemsSegment = itemsSegment(hasItems)
            val segmentDocuments = documentsSegment(hasDocuments)
            val requiredSegment = requiredSegment(isFully)
            return "$PATH_PREFIX_PN/$itemsSegment/$segmentDocuments/pn_${itemsSegment}_${segmentDocuments}_${requiredSegment}.json"
        }

        fun cn(pmd: ProcurementMethod, hasAuctions: Boolean, hasDocuments: Boolean, isFully: Boolean = true): String {
            val pmdSegment = pmdSegment(pmd)
            val auctionsSegment = auctionSegment(hasAuctions)
            val segmentDocuments = documentsSegment(hasDocuments)
            val requiredSegment = requiredSegment(isFully)
            return "$PATH_PREFIX_CN/$pmdSegment/$auctionsSegment/$segmentDocuments/cn_${auctionsSegment}_${segmentDocuments}_${requiredSegment}.json"
        }
    }

    object Pn {
        val PATH_PREFIX_RESPONSE = "json/dto/create/pn/response"

        fun response(hasItems: Boolean, hasDocuments: Boolean, isFully: Boolean = true): String {
            val itemsSegment = itemsSegment(hasItems)
            val segmentDocuments = documentsSegment(hasDocuments)
            val requiredSegment = requiredSegment(isFully)
            return "$PATH_PREFIX_RESPONSE/$itemsSegment/$segmentDocuments/pn_${itemsSegment}_${segmentDocuments}_${requiredSegment}.json"
        }
    }

    object CnOnPn {
        val PATH_PREFIX_REQUEST = "json/dto/create/cn_on_pn/request"
        val PATH_PREFIX_RESPONSE = "json/dto/create/cn_on_pn/response"

        fun request(
            pmd: ProcurementMethod,
            hasAuctions: Boolean,
            hasDocuments: Boolean,
            isFully: Boolean = true
        ): String {
            val pmdSegment = pmdSegment(pmd)
            val auctionsSegment = auctionSegment(hasAuctions)
            val segmentDocuments = documentsSegment(hasDocuments)
            val requiredSegment = requiredSegment(isFully)
            return "$PATH_PREFIX_REQUEST/$pmdSegment/$auctionsSegment/$segmentDocuments/request_cn_${auctionsSegment}_${segmentDocuments}_on_pn_${requiredSegment}.json"
        }

        fun response(
            pmd: ProcurementMethod,
            hasAuctions: Boolean,
            hasItems: Boolean,
            hasDocuments: Boolean,
            isFully: Boolean = true
        ): String {
            val pmdSegment = pmdSegment(pmd)
            val auctionsSegment = auctionSegment(hasAuctions)
            val itemsSegment = itemsSegment(hasItems)
            val segmentDocuments = documentsSegment(hasDocuments)
            val requiredSegment = requiredSegment(isFully)
            return "$PATH_PREFIX_RESPONSE/$pmdSegment/$auctionsSegment/$itemsSegment/$segmentDocuments/response_cn_${auctionsSegment}_on_pn_${itemsSegment}_${segmentDocuments}_${requiredSegment}.json"
        }
    }

    fun pmdSegment(pmd: ProcurementMethod): String = when (pmd) {
        ProcurementMethod.OT, ProcurementMethod.TEST_OT,
        ProcurementMethod.SV, ProcurementMethod.TEST_SV,
        ProcurementMethod.MV, ProcurementMethod.TEST_MV -> "op"

        ProcurementMethod.DA, ProcurementMethod.TEST_DA,
        ProcurementMethod.NP, ProcurementMethod.TEST_NP,
        ProcurementMethod.OP, ProcurementMethod.TEST_OP -> "lp"

        ProcurementMethod.RT, ProcurementMethod.TEST_RT,
        ProcurementMethod.FA, ProcurementMethod.TEST_FA -> throw IllegalArgumentException()
    }

    private fun auctionSegment(hasAuctions: Boolean): String =
        if (hasAuctions) "with_auctions" else "without_auctions"

    private fun itemsSegment(hasItems: Boolean): String =
        if (hasItems) "with_items" else "without_items"

    private fun documentsSegment(hasDocuments: Boolean): String =
        if (hasDocuments) "with_documents" else "without_documents"

    fun requiredSegment(isFully: Boolean): String =
        if (isFully) "full" else "required"
}