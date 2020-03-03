package com.procurement.access.infrastructure.handler

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.domain.util.Action

interface Handler<T : Action, R: Any> {
    val action: T
    fun handle(node: JsonNode): R
}
