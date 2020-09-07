package com.procurement.access.json

object JsonFilePathGenerator {

    fun auctionSegment(hasAuctions: Boolean): String =
        if (hasAuctions) "with_auctions" else "without_auctions"

    fun itemsSegment(hasItems: Boolean): String =
        if (hasItems) "with_items" else "without_items"

    fun documentsSegment(hasDocuments: Boolean): String =
        if (hasDocuments) "with_documents" else "without_documents"
}