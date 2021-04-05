package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.OperationType

data class CreateRelationToContractProcessStageParams(
    val cpid: Cpid,
    val ocid: Ocid,
    val relatedOcid: Ocid,
    val operationType: OperationType
)