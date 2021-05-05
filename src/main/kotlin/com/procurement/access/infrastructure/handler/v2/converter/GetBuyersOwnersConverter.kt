package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.params.GetBuyersOwnersParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetBuyersOwnersRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

fun GetBuyersOwnersRequest.convert(): Result<GetBuyersOwnersParams, DataErrors> =
    GetBuyersOwnersParams(
        cpid = parseCpid(cpid).onFailure { return it },
        ocid = parseOcid(ocid).onFailure { return it }
    ).asSuccess()
