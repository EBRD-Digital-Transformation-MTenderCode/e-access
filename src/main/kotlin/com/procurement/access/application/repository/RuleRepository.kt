package com.procurement.access.application.repository

import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.lib.functional.Result

interface RuleRepository {

    fun find(
        country: String,
        pmd: ProcurementMethod,
        operationType: String,
        parameter: String
    ): Result<String?, Fail.Incident.Database>

    fun find(
        country: String,
        pmd: ProcurementMethod,
        operationType: OperationType,
        parameter: String
    ): Result<String?, Fail.Incident.Database> = find(country, pmd, operationType.key, parameter)
}
