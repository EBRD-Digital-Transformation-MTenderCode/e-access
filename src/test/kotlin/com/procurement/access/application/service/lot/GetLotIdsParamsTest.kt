package com.procurement.access.application.service.lot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetLotIdsParamsTest {


    @Test
    fun testSorting() {
        val unsortedStates = listOf(
            createState(status = null, statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = null, statusDetails = "awarded"),
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "complete", statusDetails = "empty"),
            createState(status = null, statusDetails = "empty")
        )
        val expected = listOf(
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "active", statusDetails = null),
            createState(status = "complete", statusDetails = "empty"),
            createState(status = null, statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatus() {
        val unsortedStates = listOf(
            createState(status = "cancelled", statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = "planned", statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = null, statusDetails = null)
        )
        val expected = listOf(
            createState(status = "active", statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = "cancelled", statusDetails = null),
            createState(status = "planned", statusDetails = null),
            createState(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatusDetails(){
        val unsortedStates = listOf(
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "cancelled"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = null),
            createState(status = null, statusDetails = "unsuccessful")
        )
        val expected = listOf(
            createState(status = null, statusDetails = "cancelled"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "unsuccessful"),
            createState(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortDuplicate() {
        val unsortedStates = listOf(
            createState(status = null, statusDetails = null),
            createState(status = "active", statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = "active", statusDetails = "awarded")
        )
        val expected = listOf(
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortNulls() {
        val unsortedStates = listOf(
            createState(status = null, statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = null, statusDetails = "empty"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = null, statusDetails = "awarded"),
            createState(status = null, statusDetails = null),
            createState(status = "complete", statusDetails = null)
        )
        val expected = listOf(
            createState(status = "active", statusDetails = "empty"),
            createState(status = "active", statusDetails = null),
            createState(status = "complete", statusDetails = null),
            createState(status = null, statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = null),
            createState(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }
    private fun createState(status :String?, statusDetails :String?) : GetLotIdsParams.State {
        return GetLotIdsParams.State.tryCreate(status,statusDetails).get
    }
}
