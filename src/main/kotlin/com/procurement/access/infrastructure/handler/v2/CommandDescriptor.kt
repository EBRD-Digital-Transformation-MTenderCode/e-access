package com.procurement.access.infrastructure.handler.v2

import com.fasterxml.jackson.databind.JsonNode
import com.procurement.access.infrastructure.api.Action
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.api.command.id.CommandId

data class CommandDescriptor(
    val version: ApiVersion,
    val id: CommandId,
    val action: Action,
    val body: Body
) {
    data class Body(val asString: String, val asJsonNode: JsonNode)

    companion object
}
