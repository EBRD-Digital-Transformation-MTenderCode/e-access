package com.procurement.access.application.repository

import com.procurement.access.domain.fail.Fail
import com.procurement.access.lib.functional.Result

interface CriteriaRepository {

    fun find(country: String, language: String): Result<String?, Fail.Incident.Database>
}
