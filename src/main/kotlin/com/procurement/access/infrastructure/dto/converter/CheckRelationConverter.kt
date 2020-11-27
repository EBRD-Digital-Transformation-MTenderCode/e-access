package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckRelationParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CheckRelationRequest
import com.procurement.access.lib.functional.Result

fun CheckRelationRequest.convert(): Result<CheckRelationParams, DataErrors> =
    CheckRelationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        relatedCpid = this.relatedCpid,
        operationType = this.operationType,
        existenceRelation = this.existenceRelation
    )
