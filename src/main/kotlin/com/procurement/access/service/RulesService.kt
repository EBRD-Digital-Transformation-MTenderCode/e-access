package com.procurement.access.service

import com.procurement.access.dao.RulesDao
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import org.springframework.stereotype.Service

@Service
class RulesService(private val rulesDao: RulesDao) {

    fun getRule(country: String, pmd: String): String {
        return rulesDao.getValue(country, pmd, RULE) ?: throw ErrorException(ErrorType.RULES_NOT_FOUND)
    }

    companion object {
        private const val RULE = "rule"
    }
}
