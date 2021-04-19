package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.TenderStatus
import com.procurement.access.domain.model.enums.TenderStatusDetails
import com.procurement.access.domain.model.lot.LotId
import com.procurement.access.infrastructure.entity.CNEntity
import com.procurement.access.infrastructure.entity.RfqEntity

data class SettedLotsStatusUnsuccessful(
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

fun SettedLotsStatusUnsuccessful.Companion.fromDomain(cn: CNEntity, data: SetLotsStatusUnsuccessfulData): SettedLotsStatusUnsuccessful =
    SettedLotsStatusUnsuccessful(
        tender = SettedLotsStatusUnsuccessful.Tender(
            status = cn.tender.status,
            statusDetails = cn.tender.statusDetails
        ),
        lots = data.lots.map { lot ->
            SettedLotsStatusUnsuccessful.Lot(
                id = lot.id,
                status = LotStatus.UNSUCCESSFUL
            )
        }
    )

fun SettedLotsStatusUnsuccessful.Companion.fromDomain(rq: RfqEntity, data: SetLotsStatusUnsuccessfulData): SettedLotsStatusUnsuccessful =
    SettedLotsStatusUnsuccessful(
        tender = SettedLotsStatusUnsuccessful.Tender(
            status = rq.tender.status,
            statusDetails = rq.tender.statusDetails
        ),
        lots = data.lots.map { lot ->
            SettedLotsStatusUnsuccessful.Lot(
                id = lot.id,
                status = LotStatus.UNSUCCESSFUL
            )
        }
    )