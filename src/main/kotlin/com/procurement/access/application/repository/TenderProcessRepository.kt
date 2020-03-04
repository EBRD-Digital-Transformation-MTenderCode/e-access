package com.procurement.access.application.repository

import com.datastax.driver.core.ResultSet
import com.procurement.access.domain.fail.incident.DatabaseIncident
import com.procurement.access.domain.util.Result
import com.procurement.access.model.entity.TenderProcessEntity
import java.util.*

interface TenderProcessRepository {

    fun save(entity: TenderProcessEntity): Result<ResultSet, DatabaseIncident>
    fun getByCpIdAndStage(cpId: String, stage: String): Result<TenderProcessEntity?, DatabaseIncident>
    fun findAuthByCpid(cpid: String): Result<List<Auth>, DatabaseIncident>
}

class Auth(val token: UUID, val owner: String)
