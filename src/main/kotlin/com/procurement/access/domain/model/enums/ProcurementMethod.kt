package com.procurement.access.domain.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.procurement.access.domain.EnumElementProvider

enum class ProcurementMethod(@JsonValue override val key: String) : EnumElementProvider.Key {
    MV("open"),
    OT("open"),
    RT("selective"),
    SV("open"),
    DA("limited"),
    NP("limited"),
    FA("limited"),
    OP("selective"),
    TEST_OT("open"),
    TEST_SV("open"),
    TEST_RT("selective"),
    TEST_MV("open"),
    TEST_DA("limited"),
    TEST_NP("limited"),
    TEST_FA("limited"),
    TEST_OP("selective");

    override fun toString(): String = key

    companion object : EnumElementProvider<ProcurementMethod>(info = info()) {

        @JvmStatic
        @JsonCreator
        fun creator(name: String) = ProcurementMethod.orThrow(name)
    }
}
