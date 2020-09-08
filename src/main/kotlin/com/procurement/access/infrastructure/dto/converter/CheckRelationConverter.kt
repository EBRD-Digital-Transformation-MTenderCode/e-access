package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CheckRelationParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.check.relation.CheckRelationRequest

fun CheckRelationRequest.convert(): Result<CheckRelationParams, DataErrors> =
    CheckRelationParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        relatedCpid = this.relatedCpid,
        operationType = this.operationType,
        existenceRelation = this.existenceRelation
    )
