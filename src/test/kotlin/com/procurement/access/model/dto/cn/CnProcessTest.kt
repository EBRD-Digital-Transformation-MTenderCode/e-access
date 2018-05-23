package com.procurement.access.model.dto.cn

import com.procurement.access.utils.compare
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CnProcessTest {
    @Test
    @DisplayName("cnProcessDto")
    fun cnProcessDto() {
        compare(CnProcess::class.java, "cn.json")
    }
}