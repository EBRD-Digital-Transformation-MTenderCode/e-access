package com.procurement.access.service

import com.procurement.access.dao.RulesDao
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.rule.MinSpecificWeightPriceRule
import com.procurement.access.domain.rule.TenderStatesRule
import com.procurement.access.exception.ErrorException
import com.procurement.access.exception.ErrorType
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.asFailure
import com.procurement.access.lib.functional.asSuccess
import com.procurement.access.utils.toObject
import com.procurement.access.utils.tryToObject
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RulesService(private val rulesDao: RulesDao) {

    companion object {
        private const val VALID_STATES_PARAMETER = "validStates"
        private const val MAX_DURATION_OF_FA_PARAMETER = "maxDurationOfFA"
        private const val MIN_SPECIFIC_WEIGHT_PRICE = "minSpecificWeightPrice"
        private const val OPERATION_TYPE_ALL = "all"
    }

    fun isAuctionRequired(
        country: String,
        pmd: ProcurementMethod,
        mainProcurementCategory: MainProcurementCategory
    ): Boolean = getValue(country, pmd, OPERATION_TYPE_ALL, mainProcurementCategory.key).toBoolean()

    fun getTenderStates(
        country: String,
        pmd: ProcurementMethod,
        operationType: OperationType
    ): Result<TenderStatesRule, Fail> {
        val states = rulesDao.getData(
            country = country,
            pmd = pmd,
            operationType = operationType,
            parameter = VALID_STATES_PARAMETER
        )
            .orForwardFail { fail -> return fail }
            ?: return ValidationErrors.TenderStatesNotFound(pmd = pmd, operationType = operationType, country = country)
                .asFailure()

        return states.toTenderStatesRule()
            .orForwardFail { fail -> return fail }
            .asSuccess()
    }

    private fun String.toTenderStatesRule(): Result<TenderStatesRule, Fail> =
        this.tryToObject(TenderStatesRule::class.java)
            .doReturn { error ->
                return Result.failure(Fail.Incident.DatabaseIncident(exception = error.exception))
            }
            .asSuccess()

    fun getMaxDurationOfFA(
        country: String,
        pmd: ProcurementMethod
    ): Duration {
        val parameterValue = getValue(country, pmd, OPERATION_TYPE_ALL, MAX_DURATION_OF_FA_PARAMETER)

        return try {
            Duration.ofSeconds(parameterValue.toLong())
        } catch (exception: NumberFormatException) {
            throw ErrorException(
                error = ErrorType.RULES_INCORRECT_FORMAT,
                message = "Rule '$MAX_DURATION_OF_FA_PARAMETER' contains incorrect value"
            )
        }
    }

    fun getMinSpecificWeightPriceLimits(
        country: String,
        pmd: ProcurementMethod
    ): MinSpecificWeightPriceRule {
        val rule = getValue(country, pmd, OPERATION_TYPE_ALL, MIN_SPECIFIC_WEIGHT_PRICE)
        return try {
            toObject(MinSpecificWeightPriceRule::class.java, rule)
        } catch (exception: Exception) {
            throw ErrorException(
                error = ErrorType.RULES_INCORRECT_FORMAT,
                message = "Rule '$MIN_SPECIFIC_WEIGHT_PRICE' contains incorrect value"
            )
        }
    }

    private fun getValue(
        country: String,
        pmd: ProcurementMethod,
        operationType: String,
        parameter: String
    ): String =
        rulesDao.getValue(country, pmd, operationType, parameter)
            ?: throw ErrorException(ErrorType.RULES_NOT_FOUND)


}
