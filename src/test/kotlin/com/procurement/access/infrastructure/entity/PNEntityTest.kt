package com.procurement.access.infrastructure.entity

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.json.JsonFilePathGenerator
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PNEntityTest : AbstractDTOTestBase<PNEntity>(PNEntity::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/entity/pn/entity_pn_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/entity/pn/entity_pn_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/entity/pn/entity_pn_required_2.json")
    }
}
