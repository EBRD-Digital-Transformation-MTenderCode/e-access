package com.procurement.access.infrastructure.bind.jackson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.procurement.access.infrastructure.bind.api.command.id.CommandIdModule
import com.procurement.access.infrastructure.bind.api.v2.incident.IncidentModule
import com.procurement.access.infrastructure.bind.api.version.ApiVersionModule
import com.procurement.access.infrastructure.bind.date.JsonDateTimeModule
import com.procurement.access.infrastructure.bind.money.MoneyModule

fun ObjectMapper.configuration() {
    this.registerModule(MoneyModule())
    this.registerModule(ApiVersionModule())
    this.registerModule(CommandIdModule())
    this.registerModule(IncidentModule())
    this.registerModule(JsonDateTimeModule())
    this.registerModule(KotlinModule())

    this.configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, true)
    this.configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    this.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false)
    this.configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false)
    this.nodeFactory = JsonNodeFactory.withExactBigDecimals(true)
}
