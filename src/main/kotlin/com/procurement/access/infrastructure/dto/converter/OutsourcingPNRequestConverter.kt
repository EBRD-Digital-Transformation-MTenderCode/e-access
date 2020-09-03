package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.OutsourcingPNParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.pn.OutsourcingPNRequest

fun OutsourcingPNRequest.convert(): Result<OutsourcingPNParams, DataErrors.Validation.DataMismatchToPattern> =
    OutsourcingPNParams.tryCreate(cpid = this.cpid, ocid = this.ocid, cpidFA = this.cpidFA)
