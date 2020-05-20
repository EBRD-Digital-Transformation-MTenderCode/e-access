package com.procurement.access.application.service.lot

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FindLotIdsParamsTest {


    @Test
    fun testSorting() {
        val unsortedStates = listOf(
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
            createState(status = null, statusDetails = "empty")
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatus() {
        val unsortedStates = listOf(
            createState(status = "cancelled", statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = "planning", statusDetails = null),
            createState(status = "active", statusDetails = null)
        )
        val expected = listOf(
            createState(status = "active", statusDetails = null),
            createState(status = "active", statusDetails = null),
            createState(status = "cancelled", statusDetails = null),
            createState(status = "planning", statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatusDetails(){
        val unsortedStates = listOf(
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "awarded")
        )
        val expected = listOf(
            createState(status = null, statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty"),
            createState(status = null, statusDetails = "empty")
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortDuplicate() {
        val unsortedStates = listOf(
            createState(status = "active", statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = "active", statusDetails = "awarded")
        )
        val expected = listOf(
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "active", statusDetails = "awarded"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = null, statusDetails = "empty")
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortNulls() {
        val unsortedStates = listOf(
            createState(status = "active", statusDetails = null),
            createState(status = null, statusDetails = "empty"),
            createState(status = "active", statusDetails = "empty"),
            createState(status = null, statusDetails = "awarded"),
            createState(status = "complete", statusDetails = null)
        )
        val expected = listOf(
            createState(status = "active", statusDetails = "empty"),
            createState(status = "active", statusDetails = null),
            createState(status = "complete", statusDetails = null),
            createState(status = null, statusDetails = "awarded"),
            createState(status = null, statusDetails = "empty")
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }
    private fun createState(status :String?, statusDetails :String?) : FindLotIdsParams.State {
        return FindLotIdsParams.State.tryCreate(status, statusDetails).get
    }
}
