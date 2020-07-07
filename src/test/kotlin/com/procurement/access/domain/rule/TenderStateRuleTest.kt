package com.procurement.access.domain.rule

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class TenderStateRuleTest : AbstractDTOTestBase<TenderStatesRule>(TenderStatesRule::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/domain/rule/tender_states_rule.json")
    }
}
