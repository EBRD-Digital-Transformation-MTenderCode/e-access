package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CreateRelationToOtherProcessParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.infrastructure.handler.create.relation.CreateRelationToOtherProcessRequest
import com.procurement.access.lib.functional.Result

fun CreateRelationToOtherProcessRequest.convert(): Result<CreateRelationToOtherProcessParams, DataErrors.Validation> =
    CreateRelationToOtherProcessParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        relatedCpid = this.relatedCpid,
        operationType = this.operationType
    )
