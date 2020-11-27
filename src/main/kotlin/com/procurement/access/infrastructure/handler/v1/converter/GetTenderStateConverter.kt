package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateParams
import com.procurement.access.infrastructure.handler.v2.model.request.GetTenderStateRequest

fun GetTenderStateRequest.convert() =
    GetTenderStateParams.tryCreate(cpid = cpid, ocid = ocid)