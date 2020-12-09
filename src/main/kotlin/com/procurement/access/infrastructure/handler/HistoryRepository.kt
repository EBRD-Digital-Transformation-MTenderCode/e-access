package com.procurement.access.infrastructure.handler

import com.procurement.access.domain.fail.Fail
import com.procurement.access.infrastructure.api.Action
import com.procurement.access.infrastructure.api.command.id.CommandId
import com.procurement.access.lib.functional.Result

interface HistoryRepository {
    fun getHistory(commandId: CommandId, action: Action): Result<String?, Fail.Incident.Database>
    fun saveHistory(commandId: CommandId, action: Action, data: String): Result<Boolean, Fail.Incident.Database>
}
