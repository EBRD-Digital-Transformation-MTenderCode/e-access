package com.procurement.access.application.model.data

data class GetItemsByLotsData(
     val lots: List<Lot>
) {
    data class Lot(
         val id: String
    )
}