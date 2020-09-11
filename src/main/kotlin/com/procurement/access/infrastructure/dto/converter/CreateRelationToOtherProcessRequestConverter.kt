package com.procurement.access.infrastructure.dto.converter

import com.procurement.access.application.model.params.CreateRelationToOtherProcessParams
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.util.Result
import com.procurement.access.infrastructure.handler.create.relation.CreateRelationToOtherProcessRequest

fun CreateRelationToOtherProcessRequest.convert(): Result<CreateRelationToOtherProcessParams, DataErrors.Validation> =
    CreateRelationToOtherProcessParams.tryCreate(
        cpid = this.cpid,
        ocid = this.ocid,
        relatedCpid = this.relatedCpid,
        operationType = this.operationType
    )
