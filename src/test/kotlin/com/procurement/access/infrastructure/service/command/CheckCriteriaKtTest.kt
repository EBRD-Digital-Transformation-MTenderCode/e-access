package com.procurement.access.infrastructure.service.command

import com.procurement.access.infrastructure.dto.cn.criteria.CriterionRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CheckCriteriaKtTest {

    @Test
    fun getRequirementGroupsCombinations_twoGroups_success() {

        val reqGroup1 =  CriterionRequest.RequirementGroup(id = "1", description = "", requirements = emptyList())
        val reqGroup2 =  CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())

        val reqGroup3 =  CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
        val reqGroup4 =  CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())

        val groupsByCriterion1 = CriterionRequest(id = "cr1", description = null, title = "", relatedItem = null, relatesTo = null, requirementGroups = listOf(reqGroup1, reqGroup2))
        val groupsByCriterion2 = CriterionRequest(id = "cr2", description = "desc", title = "", relatedItem = null, relatesTo = null, requirementGroups =  listOf(reqGroup3, reqGroup4))

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

        val reqGroup1 =  CriterionRequest.RequirementGroup(id = "1", description = "", requirements = emptyList())
        val reqGroup2 =  CriterionRequest.RequirementGroup(id = "2", description = "", requirements = emptyList())

        val reqGroup3 =  CriterionRequest.RequirementGroup(id = "3", description = "", requirements = emptyList())
        val reqGroup4 =  CriterionRequest.RequirementGroup(id = "4", description = "", requirements = emptyList())
        val reqGroup5 =  CriterionRequest.RequirementGroup(id = "5", description = "", requirements = emptyList())

        val reqGroup6 =  CriterionRequest.RequirementGroup(id = "6", description = "", requirements = emptyList())


        val groupsByCriterion1 = CriterionRequest(id = "cr1", description = null, title = "", relatedItem = null, relatesTo = null, requirementGroups = listOf(reqGroup1, reqGroup2))
        val groupsByCriterion2 = CriterionRequest(id = "cr2", description = "desc", title = "", relatedItem = null, relatesTo = null, requirementGroups =  listOf(reqGroup3, reqGroup4, reqGroup5))
        val groupsByCriterion3 = CriterionRequest(
            id = "cr3",
            description = null,
            title = "",
            relatedItem = null,
            relatesTo = null,
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
}