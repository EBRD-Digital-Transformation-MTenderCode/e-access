package com.procurement.access.infrastructure.repository

object Database {
    const val KEYSPACE_ACCESS = "access"
    const val KEYSPACE_OLD = "ocds"

    object HistoryOld {
        const val TABLE = "access_history"
        const val COMMAND_ID = "operation_id"
        const val COMMAND_NAME = "command"
        const val COMMAND_DATE = "operation_date"
        const val JSON_DATA = "json_data"
    }

    object HistoryNew {
        const val TABLE = "history"
        const val COMMAND_ID = "operation_id"
        const val COMMAND_NAME = "command"
        const val COMMAND_DATE = "operation_date"
        const val JSON_DATA = "json_data"
    }

    object Rules {
        const val TABLE = "rules"
        const val COUNTRY = "country"
        const val PMD = "pmd"
        const val OPERATION_TYPE = "operation_type"
        const val PARAMETER = "parameter"
        const val VALUE = "value"
    }

    object Tender {
        const val TABLE = "tenders"
        const val CPID = "cpid"
        const val OCID = "ocid"
        const val TOKEN = "token_entity"
        const val CREATION_DATE = "created_date"
        const val OWNER = "owner"
        const val JSON_DATA = "json_data"
    }

    object Criteria {
        const val TABLE = "criteria_template"
        const val COUNTRY = "country"
        const val LANGUAGE = "language"
        const val JSON_DATA = "json_data"
    }
}
