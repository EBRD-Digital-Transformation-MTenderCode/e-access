package com.procurement.access.application.service.lot

import com.procurement.access.domain.model.enums.LotStatus
import com.procurement.access.domain.model.enums.LotStatusDetails
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GetLotIdsParamsTest {


    @Test
    fun testSorting() {
        val unsortedStates = listOf(
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = LotStatus.COMPLETE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY)
        )
        val expected = listOf(
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.COMPLETE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatus() {
        val unsortedStates = listOf(
            GetLotIdsParams.State(status = LotStatus.CANCELLED, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.PLANNED, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )
        val expected = listOf(
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.CANCELLED, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.PLANNED, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortByLotStatusDetails(){
        val unsortedStates = listOf(
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.CANCELLED),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.UNSUCCESSFUL)
        )
        val expected = listOf(
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.CANCELLED),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.UNSUCCESSFUL),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortDuplicate() {
        val unsortedStates = listOf(
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED)
        )
        val expected = listOf(
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }

    @Test
    fun testSortNulls() {
        val unsortedStates = listOf(
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.COMPLETE, statusDetails = null)
        )
        val expected = listOf(
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = LotStatus.ACTIVE, statusDetails = null),
            GetLotIdsParams.State(status = LotStatus.COMPLETE, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.AWARDED),
            GetLotIdsParams.State(status = null, statusDetails = LotStatusDetails.EMPTY),
            GetLotIdsParams.State(status = null, statusDetails = null),
            GetLotIdsParams.State(status = null, statusDetails = null)
        )

        val sortedStates = unsortedStates.sorted()
        assertEquals(expected, sortedStates)
    }
}
