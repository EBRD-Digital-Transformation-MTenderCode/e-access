package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.service.tender.strategy.get.state.GetTenderStateParams
import com.procurement.access.infrastructure.handler.get.tender.state.GetTenderStateRequest

fun GetTenderStateRequest.convert() =
    GetTenderStateParams.tryCreate(cpid = cpid, ocid = ocid)