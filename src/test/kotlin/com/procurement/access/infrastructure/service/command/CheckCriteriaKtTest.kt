package com.procurement.access.infrastructure.service.command

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import com.procurement.access.domain.model.coefficient.CoefficientRate
import com.procurement.access.domain.model.coefficient.CoefficientValue
import com.procurement.access.domain.model.enums.ConversionsRelatesTo
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.MainProcurementCategory
import com.procurement.access.domain.model.enums.ProcurementMethod
import com.procurement.access.domain.model.enums.RequirementDataType
import com.procurement.access.domain.model.option.RelatedOption
import com.procurement.access.domain.model.requirement.EligibleEvidence
import com.procurement.access.domain.model.requirement.EligibleEvidenceType
import com.procurement.access.domain.model.requirement.ExpectedValue
import com.procurement.access.domain.model.requirement.Requirement
import com.procurement.access.domain.rule.MinSpecificWeightPriceRule
import com.procurement.access.exception.ErrorException
import com.procurement.access.infrastructure.handler.v1.model.request.ConversionRequest
import com.procurement.access.infrastructure.handler.v1.model.request.ItemReferenceRequest
import com.procurement.access.infrastructure.handler.v1.model.request.criterion.CriterionClassificationRequest
import com.procurement.access.infrastructure.handler.v1.model.request.criterion.CriterionRequest
import com.procurement.access.infrastructure.handler.v1.model.request.document.RelatedDocumentRequest
import com.procurement.access.service.RulesService
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

internal class CheckCriteriaKtTest {

    @Test
    fun getRequirementGroupsCombinations_twoGroups_success() {

        val reqGroup1 = CriterionRequest.RequirementGroup(id = "1", description = "", requirements = emptyList())
        val reqGroup2 = CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())

        val reqGroup3 = CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
        val reqGroup4 = CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())

        val groupsByCriterion1 = CriterionRequest(
            id = "cr1",
            description = null,
            title = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            relatedItem = null,
            relatesTo = CriteriaRelatesTo.TENDER,
            requirementGroups = listOf(reqGroup1, reqGroup2)
        )
        val groupsByCriterion2 = CriterionRequest(
            id = "cr2",
            description = "desc",
            title = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            relatedItem = null,
            relatesTo = CriteriaRelatesTo.TENDER,
            requirementGroups = listOf(reqGroup3, reqGroup4)
        )

        val groupsByCriteria = listOf(groupsByCriterion1, groupsByCriterion2)
        val actual = getRequirementGroupsCombinations(groupsByCriteria)

        val expected = setOf(
            listOf(reqGroup1, reqGroup3),
            listOf(reqGroup1, reqGroup4),
            listOf(reqGroup2, reqGroup3),
            listOf(reqGroup2, reqGroup4)
        )

        assertTrue(actual.size == 4)
        assertEquals(expected, actual.toSet())
    }

    @Test
    fun getRequirementGroupsCombinations_threeGroups_success() {

        val reqGroup1 = CriterionRequest.RequirementGroup(id = "1", description = "", requirements = emptyList())
        val reqGroup2 = CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())

        val reqGroup3 = CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
        val reqGroup4 = CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())
        val reqGroup5 = CriterionRequest.RequirementGroup(id = "5", description = "", requirements = emptyList())

        val reqGroup6 = CriterionRequest.RequirementGroup(id = "6", description = "", requirements = emptyList())

        val groupsByCriterion1 = CriterionRequest(
            id = "cr1",
            description = null,
            title = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            relatedItem = null,
            relatesTo = CriteriaRelatesTo.TENDER,
            requirementGroups = listOf(reqGroup1, reqGroup2)
        )
        val groupsByCriterion2 = CriterionRequest(
            id = "cr2",
            description = "desc",
            title = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            relatedItem = null,
            relatesTo = CriteriaRelatesTo.TENDER,
            requirementGroups = listOf(reqGroup3, reqGroup4, reqGroup5)
        )
        val groupsByCriterion3 = CriterionRequest(
            id = "cr3",
            description = null,
            title = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            relatedItem = null,
            relatesTo = CriteriaRelatesTo.TENDER,
            requirementGroups = listOf(reqGroup6)
        )

        val expected = setOf(
            listOf(reqGroup1, reqGroup3, reqGroup6),
            listOf(reqGroup1, reqGroup4, reqGroup6),
            listOf(reqGroup1, reqGroup5, reqGroup6),
            listOf(reqGroup2, reqGroup3, reqGroup6),
            listOf(reqGroup2, reqGroup4, reqGroup6),
            listOf(reqGroup2, reqGroup5, reqGroup6)
        )

        val groupsByCriteria = listOf(groupsByCriterion1, groupsByCriterion2, groupsByCriterion3)
        val actual = getRequirementGroupsCombinations(groupsByCriteria)

        assertTrue(actual.size == 6)
        assertEquals(expected, actual.toSet())
    }

    @Test
    fun getCriteriaCombinations() {
        val tenderCriterion = CriterionRequest(
            id = "tenderCriterion",
            relatesTo = CriteriaRelatesTo.TENDER,
            relatedItem = null,
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = emptyList()
        )

        val tendererCriterion = CriterionRequest(
            id = "tendererCriterion",
            relatesTo = CriteriaRelatesTo.TENDERER,
            relatedItem = null,
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = emptyList()
        )

        val lotCriterion1 = CriterionRequest(
            id = "lotCriterion1",
            relatesTo = CriteriaRelatesTo.LOT,
            relatedItem = "lot1",
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = emptyList()
        )

        val lotCriterion2 = CriterionRequest(
            id = "lotCriterion2",
            relatesTo = CriteriaRelatesTo.LOT,
            relatedItem = "lot2",
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = emptyList()
        )

        val itemCriterion1 = CriterionRequest(
            id = "itemsCriterion1",
            relatesTo = CriteriaRelatesTo.ITEM,
            relatedItem = "item1",
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = emptyList()
        )

        val criteria = listOf(tenderCriterion, tendererCriterion, lotCriterion1, lotCriterion2, itemCriterion1)
        val items = listOf(
            ItemReferenceRequest(id = "item1", relatedLot = "lot1"),
            ItemReferenceRequest(id = "item2", relatedLot = "lot2")
        )

        val actual = getCriteriaCombinations(criteria, items).map { it.toSet() }.toSet()
        val expected = setOf(
            setOf(tenderCriterion, tendererCriterion, lotCriterion1, itemCriterion1),
            setOf(tenderCriterion, tendererCriterion, lotCriterion2)
        )
        assertEquals(expected, actual)
    }

    @Test
    fun calculateAndCheckMinSpecificWeightPrice_success() {
        val mainProcurementCategory = MainProcurementCategory.WORKS
        val pmd = ProcurementMethod.CD
        val country = "MD"
        val rulesService: RulesService = mock()

        val tenderCriterion = getTenderCriterion()
        val tendererCriterion = getTendererCriterion()
        val lotsCriteria = getLotsCriteria()

        val criteria = listOf(tenderCriterion, tendererCriterion, lotsCriteria[0], lotsCriteria[1])
        val conversions = getConversions()

        val items = listOf(
            ItemReferenceRequest(id = "", relatedLot = lotsCriteria[0].relatedItem!!),
            ItemReferenceRequest(id = "", relatedLot = lotsCriteria[1].relatedItem!!)
        )

        val limit = 0.2.toBigDecimal()
        whenever(rulesService.getMinSpecificWeightPriceLimits(country, pmd))
            .thenReturn(
                MinSpecificWeightPriceRule(
                    goods = BigDecimal.ZERO,
                    services = BigDecimal.ZERO,
                    works = limit
                )
            )

        assertDoesNotThrow {
            calculateAndCheckMinSpecificWeightPrice(
                mainProcurementCategory, criteria, conversions, items, rulesService, pmd, country
            )
        }
    }

    @Test
    fun calculateAndCheckMinSpecificWeightPrice_lessThanLimit_fail() {
        val mainProcurementCategory = MainProcurementCategory.WORKS
        val pmd = ProcurementMethod.CD
        val country = "MD"
        val rulesService: RulesService = mock()

        val tenderCriterion = getTenderCriterion()
        val tendererCriterion = getTendererCriterion()
        val lotsCriteria = getLotsCriteria()

        val criteria = listOf(tenderCriterion, tendererCriterion, lotsCriteria[0], lotsCriteria[1])
        val conversions = getConversions()

        val items = listOf(
            ItemReferenceRequest(id = "", relatedLot = lotsCriteria[0].relatedItem!!),
            ItemReferenceRequest(id = "", relatedLot = lotsCriteria[1].relatedItem!!)
        )

        val limit = 0.69.toBigDecimal()
        whenever(rulesService.getMinSpecificWeightPriceLimits(country, pmd))
            .thenReturn(
                MinSpecificWeightPriceRule(
                    goods = BigDecimal.ZERO,
                    services = BigDecimal.ZERO,
                    works = limit
                )
            )

        val result = assertThrows<ErrorException> {
            calculateAndCheckMinSpecificWeightPrice(
                mainProcurementCategory, criteria, conversions, items, rulesService, pmd, country
            )
        }
        val expectedError = "Invalid conversion value. Minimal price share of requirements 'req1, req2' must be greater than 0.69. Actual value: '0.21'."
        assertEquals(expectedError, result.message)
    }

    private fun getLotsCriteria(): List<CriterionRequest> {
        val reqGroup5 = CriterionRequest.RequirementGroup(
            id = "5", description = "", requirements = listOf(
                Requirement(
                    id = "req2",
                    description = null,
                    period = null,
                    title = "",
                    dataType = RequirementDataType.STRING,
                    value = ExpectedValue.AsString(""),
                    eligibleEvidences = emptyList()
                )
            )
        )
        val lotCriterion1 = CriterionRequest(
            id = "lotCriterion1",
            relatesTo = CriteriaRelatesTo.LOT,
            relatedItem = "lot1",
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = listOf(reqGroup5)
        )

        val reqGroup6 = CriterionRequest.RequirementGroup(id = "6", description = "", requirements = emptyList())
        val lotCriterion2 = CriterionRequest(
            id = "lotCriterion2",
            relatesTo = CriteriaRelatesTo.LOT,
            relatedItem = "lot2",
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = listOf(reqGroup6)
        )
        return listOf(lotCriterion1, lotCriterion2)
    }

    private fun getTendererCriterion(): CriterionRequest {
        val reqGroup3 = CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
        val reqGroup4 = CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())
        val tendererCriterion = CriterionRequest(
            id = "tendererCriterion",
            relatesTo = CriteriaRelatesTo.TENDERER,
            relatedItem = null,
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = listOf(reqGroup3, reqGroup4)
        )
        return tendererCriterion
    }

    private fun getTenderCriterion(): CriterionRequest {
        val reqGroup1 = CriterionRequest.RequirementGroup(
            id = "1",
            description = "",
            requirements = listOf(
                Requirement(
                    id = "req1",
                    description = null,
                    period = null,
                    title = "",
                    dataType = RequirementDataType.STRING,
                    value = ExpectedValue.AsString(""),
                    eligibleEvidences = listOf(
                        EligibleEvidence(
                            id = UUID.randomUUID().toString(),
                            title = "title",
                            description = "description",
                            type = EligibleEvidenceType.DOCUMENT,
                            relatedDocument = RelatedDocumentRequest(id = "document")
                        )
                    )
                )
            )
        )
        val reqGroup2 = CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())
        val tenderCriterion = CriterionRequest(
            id = "tenderCriterion",
            relatesTo = CriteriaRelatesTo.TENDER,
            relatedItem = null,
            title = "",
            description = "",
            classification = CriterionClassificationRequest(
                id = "CRITERION.OTHER.123456",
                scheme = "scheme"
            ),
            requirementGroups = listOf(reqGroup1, reqGroup2)
        )
        return tenderCriterion
    }

    private fun getConversions(): List<ConversionRequest> {
        val conversion1 = ConversionRequest(
            id = "1",
            description = null,
            relatedItem = "req1",
            relatesTo = ConversionsRelatesTo.REQUIREMENT,
            rationale = "",
            coefficients = listOf(
                ConversionRequest.Coefficient(
                    id = "c1",
                    value = CoefficientValue.of(12),
                    coefficient = CoefficientRate(0.8.toBigDecimal()),
                    relatedOption = RelatedOption()
                ),
                ConversionRequest.Coefficient(
                    id = "c2",
                    value = CoefficientValue.of(12),
                    coefficient = CoefficientRate(0.7.toBigDecimal()),
                    relatedOption = RelatedOption()
                )

            )
        )

        val conversion2 = ConversionRequest(
            id = "2",
            description = null,
            relatedItem = "req2",
            relatesTo = ConversionsRelatesTo.REQUIREMENT,
            rationale = "",
            coefficients = listOf(
                ConversionRequest.Coefficient(
                    id = "c1",
                    value = CoefficientValue.of(12),
                    coefficient = CoefficientRate(0.5.toBigDecimal()),
                    relatedOption = RelatedOption()
                ),
                ConversionRequest.Coefficient(
                    id = "c2",
                    value = CoefficientValue.of(12),
                    coefficient = CoefficientRate(0.3.toBigDecimal()),
                    relatedOption = RelatedOption()
                )

            )
        )

        return listOf(conversion1, conversion2)
    }
}
