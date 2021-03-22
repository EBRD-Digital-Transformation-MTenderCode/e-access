package com.procurement.access.infrastructure.handler.v2.converter

import com.procurement.access.application.model.params.AddClientsToPartiesInAPParams
import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.AddClientsToPartiesInAPRequest
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asSuccess

fun AddClientsToPartiesInAPRequest.convert(): Result<AddClientsToPartiesInAPParams, DataErrors> =
    AddClientsToPartiesInAPParams(
        cpid = parseCpid(cpid).onFailure { return it },
        ocid = parseOcid(ocid).onFailure { return it },
        relatedCpid = parseCpid(relatedCpid).onFailure { return it },
        relatedOcid = parseOcid(relatedOcid).onFailure { return it }
    ).asSuccess()
