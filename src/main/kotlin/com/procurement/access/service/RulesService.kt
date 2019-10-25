package com.procurement.access.service

import com.procurement.access.dao.RulesDao
import com.procurement.access.domain.model.mainProcurementCategory.MainProcurementCategory
import com.procurement.access.domain.model.procurementMethod.ProcurementMethod
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import org.springframework.stereotype.Service

@Service
class RulesService(private val rulesDao: RulesDao) {

    fun isAuctionRequired(
        country: String,
        pmd: ProcurementMethod,
        mainProcurementCategory: MainProcurementCategory
    ): Boolean = getValue(country, pmd, mainProcurementCategory.value).toBoolean()

    private fun getValue(country: String, pmd: ProcurementMethod, parameter: String): String =
        rulesDao.getValue(country, pmd, parameter)
            ?: throw ErrorException(ErrorType.RULES_NOT_FOUND)
}
