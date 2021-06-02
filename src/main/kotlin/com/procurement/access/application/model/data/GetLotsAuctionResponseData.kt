package com.procurement.access.application.model.data

import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.entity.RfqEntity
import com.procurement.access.model.dto.ocds.Lot
import com.procurement.access.model.dto.ocds.Tender
import java.math.BigDecimal

data class GetLotsAuctionResponseData(
    val tender: Tender
) { companion object {}

    data class Tender(
        val id: String,
        val title: String,
        val description: String,
        val lots: List<Lot>,
        val value: Value?
    ) {
        data class Lot(
            val id: LotId,
            val title: String,
            val description: String,
            val value: Value?
        ) { companion object {}

            data class Value(
                val amount: BigDecimal?,
                val currency: String
            )
        }

        data class Value(
            val currency: String
        )
    }
}

fun GetLotsAuctionResponseData.Tender.Lot.Companion.fromDomain(lot: Lot) =
    GetLotsAuctionResponseData.Tender.Lot(
        id = LotId.fromString(lot.id),
        title = lot.title!!,
        description = lot.description!!,
        value = lot.value.let { value ->
            GetLotsAuctionResponseData.Tender.Lot.Value(
                amount = value.amount,
                currency = value.currency
            )
        }
    )

fun GetLotsAuctionResponseData.Companion.fromDomain(tender: Tender, activeLots: List<GetLotsAuctionResponseData.Tender.Lot>) =
    GetLotsAuctionResponseData(
        tender = GetLotsAuctionResponseData.Tender(
            id = tender.id!!,
            title = tender.title,
            description = tender.description,
            lots = activeLots,
            value = null
        )
    )

fun GetLotsAuctionResponseData.Tender.Lot.Companion.fromDomain(lot: RfqEntity.Tender.Lot) =
    GetLotsAuctionResponseData.Tender.Lot(
        id = lot.id,
        title = lot.title,
        description = lot.description!!,
        value = null
    )

fun GetLotsAuctionResponseData.Companion.fromDomain(tender: RfqEntity.Tender, activeLots: List<GetLotsAuctionResponseData.Tender.Lot>) =
    GetLotsAuctionResponseData(
        tender = GetLotsAuctionResponseData.Tender(
            id = tender.id,
            title = tender.title,
            description = tender.description,
            lots = activeLots,
            value = tender.value.let { value ->
                GetLotsAuctionResponseData.Tender.Value(currency = value.currency)
            }
        )
    )