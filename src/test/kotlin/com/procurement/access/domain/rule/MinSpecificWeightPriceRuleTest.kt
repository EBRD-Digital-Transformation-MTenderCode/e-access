package com.procurement.access.domain.rule

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class MinSpecificWeightPriceRuleTest : AbstractDTOTestBase<MinSpecificWeightPriceRule>(MinSpecificWeightPriceRule::class.java) {

    @Test
    fun fully() {
        testBindingAndMapping("json/domain/rule/min_specific_weight_price_rule.json")
    }
}
