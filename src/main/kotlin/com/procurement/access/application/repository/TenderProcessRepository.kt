package com.procurement.access.application.repository

import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.enums.Stage
import com.procurement.access.lib.functional.Result
import com.procurement.access.model.entity.TenderProcessEntity

interface TenderProcessRepository {

    fun update(entity: TenderProcessEntity): Result<Boolean, Fail.Incident>
    fun save(entity: TenderProcessEntity): Result<Boolean, Fail.Incident.Database>
    fun getByCpIdAndStage(cpid: Cpid, stage: Stage): Result<TenderProcessEntity?, Fail.Incident.Database>
    fun getByCpIdAndOcid(cpid: Cpid, ocid: Ocid): Result<TenderProcessEntity?, Fail.Incident.Database>
}
