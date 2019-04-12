package com.procurement.access.infrastructure.entity

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CNEntityTest : AbstractDTOTestBase<CNEntity>(CNEntity::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/entity/cn/entity_cn_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/entity/cn/entity_cn_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/entity/cn/entity_cn_required_2.json")
    }
}
