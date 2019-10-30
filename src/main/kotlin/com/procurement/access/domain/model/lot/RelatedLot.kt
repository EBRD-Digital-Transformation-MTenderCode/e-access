package com.procurement.access.domain.model.lot

interface RelatedLot<ID> {
    val relatedLot: ID

    fun validation(lotsIds: Set<ID>): Boolean = relatedLot in lotsIds

    fun <E : RuntimeException> validation(lotsIds: Set<ID>, block: (ID) -> E) {
        if (relatedLot !in lotsIds)
            throw block(relatedLot)
    }
}
