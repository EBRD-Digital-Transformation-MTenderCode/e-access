package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckExistenceSignAuctionParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CheckExistenceSignAuctionRequest
import com.procurement.access.lib.functional.Result

fun CheckExistenceSignAuctionRequest.convert(): Result<CheckExistenceSignAuctionParams, DataErrors> = CheckExistenceSignAuctionParams.tryCreate(
    cpid = this.cpid,
    ocid = this.ocid,
    tender = tender?.convert()?.onFailure { fail -> return fail }
)

private fun CheckExistenceSignAuctionRequest.Tender.convert() = CheckExistenceSignAuctionParams.Tender.tryCreate(
    procurementMethodModalities = procurementMethodModalities
)