package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.FindAuctionsParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.find.auction.FindAuctionsRequest

fun FindAuctionsRequest.convert(): Result<FindAuctionsParams, DataErrors> =
    FindAuctionsParams.tryCreate(cpid, ocid)

