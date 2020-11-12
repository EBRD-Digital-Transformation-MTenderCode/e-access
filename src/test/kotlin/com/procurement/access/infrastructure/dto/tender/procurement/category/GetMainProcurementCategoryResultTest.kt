package com.procurement.access.infrastructure.dto.tender.procurement.category

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.infrastructure.handler.get.tender.procurement.GetMainProcurementCategoryResult
import org.junit.jupiter.api.Test

class GetMainProcurementCategoryResultTest
    : AbstractDTOTestBase<GetMainProcurementCategoryResult>(GetMainProcurementCategoryResult::class.java) {
    @Test
    fun test() {
        testBindingAndMapping("json/dto/tender/procurement/category/response_get_tender_main_procurement_category_fully.json")
    }
}
