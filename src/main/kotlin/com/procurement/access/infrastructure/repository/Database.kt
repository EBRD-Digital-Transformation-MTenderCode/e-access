package com.procurement.access.infrastructure.repository

object Database {
    const val KEYSPACE = "ocds"

    object History {
        const val TABLE = "access_history"
        const val COMMAND_ID = "operation_id"
        const val COMMAND_NAME = "command"
        const val COMMAND_DATE = "operation_date"
        const val JSON_DATA = "json_data"
    }
}
