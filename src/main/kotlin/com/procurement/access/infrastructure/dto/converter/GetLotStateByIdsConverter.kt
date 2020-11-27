package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.lot.GetLotStateByIdsParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetLotStateByIdsRequest
import com.procurement.access.lib.functional.Result

fun GetLotStateByIdsRequest.convert(): Result<GetLotStateByIdsParams, DataErrors> =
    GetLotStateByIdsParams.tryCreate(cpid = this.cpid, ocid = this.ocid, lotIds = this.lotIds)
