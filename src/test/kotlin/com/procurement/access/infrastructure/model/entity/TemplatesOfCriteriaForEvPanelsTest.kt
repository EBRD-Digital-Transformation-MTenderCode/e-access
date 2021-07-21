package com.procurement.access.infrastructure.model.entity

import com.procurement.access.infrastructure.AbstractDTOTestBase
import com.procurement.access.model.entity.TemplatesOfCriteriaForEvPanels
import org.junit.jupiter.api.Test

class TemplatesOfCriteriaForEvPanelsTest :
    AbstractDTOTestBase<TemplatesOfCriteriaForEvPanels>(TemplatesOfCriteriaForEvPanels::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/infrastructure/model/entity/template_of_criteria_fully.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/infrastructure/model/entity/template_of_criteria_required_1.json")
    }
}
