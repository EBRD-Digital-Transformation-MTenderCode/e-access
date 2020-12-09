package com.procurement.access.infrastructure.handler.v1.converter

import com.procurement.access.application.model.params.CreateRelationToOtherProcessParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.v2.model.request.CreateRelationToOtherProcessRequest
import com.procurement.access.lib.functional.Result

fun CreateRelationToOtherProcessRequest.convert(): Result<CreateRelationToOtherProcessParams, DataErrors.Validation> =
    CreateRelationToOtherProcessParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        relatedCpid = this.relatedCpid,
        relatedOcid = this.relatedOcid,
        operationType = this.operationType
    )
