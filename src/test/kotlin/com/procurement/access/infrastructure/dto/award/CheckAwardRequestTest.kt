package com.procurement.access.infrastructure.dto.award

import com.procurement.access.infrastructure.AbstractDTOTestBase
import org.junit.jupiter.api.Test

class CheckAwardRequestTest : AbstractDTOTestBase<CheckAwardRequest>(CheckAwardRequest::class.java) {
    @Test
    fun fully() {
        testBindingAndMapping("json/dto/check/award/lp/request/request_check_award_full.json")
    }

    @Test
    fun required1() {
        testBindingAndMapping("json/dto/check/award/lp/request/request_check_award_required_1.json")
    }

    @Test
    fun required2() {
        testBindingAndMapping("json/dto/check/award/lp/request/request_check_award_required_2.json")
    }
}
