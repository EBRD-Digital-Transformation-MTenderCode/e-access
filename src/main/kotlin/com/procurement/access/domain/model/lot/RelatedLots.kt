package com.procurement.access.domain.model.lot

interface RelatedLots<ID> {
    val relatedLots: List<ID>

    fun validation(lotsIds: Set<ID>): Boolean {
        relatedLots.forEach { relatedLot ->
            if (relatedLot !in lotsIds)
                return false
        }
        return true
    }

    fun <T : Exception> validation(lotsIds: Set<ID>, block: (ID) -> T) {
        relatedLots.forEach { relatedLot ->
            if (relatedLot !in lotsIds) throw block(relatedLot)
        }
    }
}
