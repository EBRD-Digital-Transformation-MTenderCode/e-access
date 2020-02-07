package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.lot.LotId

data class GetLotIdsByStatesResult(
    val lotIds: List<LotId>
)
