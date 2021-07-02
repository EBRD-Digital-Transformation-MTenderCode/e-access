package com.procurement.access.service

import com.procurement.access.application.repository.RuleRepository
import com.procurement.access.domain.fail.Fail
import com.procurement.access.domain.fail.error.ValidationErrors
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.OperationType
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.rule.LotStatesRule
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
class RulesService(
    private val ruleRepository: RuleRepository
) {

    companion object {
        private const val VALID_STATES_PARAMETER = "validStates"
        private const val VALID_LOT_STATES_PARAMETER = "validLotStates"
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
        val states = ruleRepository.find(
            country = country,
            pmd = pmd,
            operationType = operationType,
            parameter = VALID_STATES_PARAMETER
        )
            .onFailure { fail -> return fail }
            ?: return ValidationErrors.RulesNotFound(VALID_STATES_PARAMETER, country, pmd, operationType)
                .asFailure()

        return states.toTenderStatesRule()
            .onFailure { fail -> return fail }
            .asSuccess()
    }

    fun getValidLotStates(
        country: String,
        pmd: ProcurementMethod,
        operationType: OperationType
    ): Result<LotStatesRule, Fail> {
        val states = ruleRepository.find(country, pmd, operationType, VALID_LOT_STATES_PARAMETER)
            .onFailure { fail -> return fail }
            ?: return ValidationErrors.RulesNotFound(VALID_LOT_STATES_PARAMETER, country, pmd, operationType)
                .asFailure()

        return states.tryToObject(LotStatesRule::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }
    }

    private fun String.toTenderStatesRule(): Result<TenderStatesRule, Fail> =
        this.tryToObject(TenderStatesRule::class.java)
            .mapFailure { Fail.Incident.DatabaseIncident(exception = it.exception) }

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
        ruleRepository.find(country, pmd, operationType, parameter)
            .orThrow { it.exception }
            ?: throw ErrorException(ErrorType.RULES_NOT_FOUND)
}
