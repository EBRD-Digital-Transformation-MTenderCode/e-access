package com.procurement.access.application.repository

import com.datastax.driver.core.ResultSet
import com.procurement.access.domain.fail.Fail

import com.procurement.access.domain.util.Result
import com.procurement.access.model.entity.TenderProcessEntity
import java.util.*

interface TenderProcessRepository {

    fun save(entity: TenderProcessEntity): Result<ResultSet, Fail.Incident.Database>
    fun getByCpIdAndStage(cpId: String, stage: String): Result<TenderProcessEntity?, Fail.Incident.Database>
    fun findAuthByCpid(cpid: String): Result<List<Auth>, Fail.Incident.Database>
}

class Auth(val token: UUID, val owner: String)
