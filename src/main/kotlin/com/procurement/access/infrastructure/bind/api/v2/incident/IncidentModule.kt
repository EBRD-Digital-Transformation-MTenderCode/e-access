package com.procurement.access.infrastructure.bind.api.v2.incident

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.access.infrastructure.api.v2.IncidentId

class IncidentModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(IncidentId::class.java, IncidentIdSerializer())
    }
}
