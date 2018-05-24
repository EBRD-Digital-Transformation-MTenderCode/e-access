package com.procurement.access.model.dto.cn

import com.procurement.access.utils.compare
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class TenderStatusResponseDtoTest {
    @Test
    @DisplayName("tenderStatusResponseDto")
    fun tenderStatusResponseDto() {
        compare(TenderStatusResponseDto::class.java, "/json/tender_status_resp.json")
    }
}