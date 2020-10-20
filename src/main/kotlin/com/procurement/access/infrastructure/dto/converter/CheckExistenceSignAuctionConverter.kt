package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckExistenceSignAuctionParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.check.auction.CheckExistenceSignAuctionRequest

fun CheckExistenceSignAuctionRequest.convert(): Result<CheckExistenceSignAuctionParams, DataErrors> = CheckExistenceSignAuctionParams.tryCreate(
    cpid = this.cpid,
    ocid = this.ocid,
    tender = tender?.convert()?.orForwardFail { fail -> return fail }
)

private fun CheckExistenceSignAuctionRequest.Tender.convert() = CheckExistenceSignAuctionParams.Tender.tryCreate(
    procurementMethodModalities = procurementMethodModalities
)