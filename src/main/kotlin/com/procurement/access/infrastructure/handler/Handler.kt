package com.procurement.access.infrastructure.handler

import com.procurement.access.infrastructure.api.Action
import com.procurement.access.infrastructure.api.ApiVersion
import com.procurement.access.infrastructure.handler.v2.CommandDescriptor

interface Handler<R : Any> {
    val version: ApiVersion
    val action: Action

    fun handle(descriptor: CommandDescriptor): R
}
