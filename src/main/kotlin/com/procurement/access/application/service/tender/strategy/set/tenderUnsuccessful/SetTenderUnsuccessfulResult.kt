package com.procurement.access.application.service.tender.strategy.set.tenderUnsuccessful

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity

data class SetTenderUnsuccessfulResult(
    val tender: Tender,
    val lots: List<Lot>
) { companion object {}
    data class Tender(
        val status: TenderStatus,
        val statusDetails: TenderStatusDetails
    )

    data class Lot(
        val id: LotId,
        val status: LotStatus
    )
}

fun SetTenderUnsuccessfulResult.Companion.fromDomain(cn: CNEntity): SetTenderUnsuccessfulResult =
    SetTenderUnsuccessfulResult(
        tender = SetTenderUnsuccessfulResult.Tender(
            status = cn.tender.status,
            statusDetails = cn.tender.statusDetails
        ),
        lots = cn.tender.lots
            .map { lot ->
                SetTenderUnsuccessfulResult.Lot(
                    id = LotId.fromString(lot.id),
                    status = lot.status
                )
            }
    )

fun SetTenderUnsuccessfulResult.Companion.fromDomain(rq: RfqEntity): SetTenderUnsuccessfulResult =
    SetTenderUnsuccessfulResult(
        tender = SetTenderUnsuccessfulResult.Tender(
            status = rq.tender.status,
            statusDetails = rq.tender.statusDetails
        ),
        lots = rq.tender.lots
            .map { lot ->
                SetTenderUnsuccessfulResult.Lot(
                    id = lot.id,
                    status = lot.status
                )
            }
    )
