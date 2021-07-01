package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.notEmptyRule
import com.procurement.access.application.model.params.GetDataForContractParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseLotId
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.GetDataForContractRequest
import com.procurement.access.lib.extension.mapResult
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.lib.functional.validate

fun GetDataForContractRequest.convert(): Result<GetDataForContractParams, DataErrors> {
    val awards = awards.validate(notEmptyRule("awards"))
        .onFailure { return it }
        .mapResult { it.convert() }
        .onFailure { return it }

    return GetDataForContractParams(
        relatedCpid = parseCpid(relatedCpid).onFailure { return it },
        relatedOcid = parseOcid(relatedOcid).onFailure { return it },
        awards = awards
    ).asSuccess()
}

fun GetDataForContractRequest.Award.convert(): Result<GetDataForContractParams.Award, DataErrors> {
    val relatedLots = relatedLots.validate(notEmptyRule("awards.relatedLots"))
        .onFailure { return it }
        .mapResult { parseLotId(it, "awards.relatedLots") }
        .onFailure { return it }

    return GetDataForContractParams.Award(relatedLots).asSuccess()
}