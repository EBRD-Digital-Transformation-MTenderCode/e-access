package com.procurement.access.infrastructure.bind.process

import com.fasterxml.jackson.databind.module.SimpleModule
import com.procurement.access.domain.model.process.RelatedProcessIdentifier

class ProcessIdentifierModule : SimpleModule() {
    companion object {
        @JvmStatic
        private val serialVersionUID = 1L
    }

    init {
        addSerializer(RelatedProcessIdentifier::class.java, ProcessIdentifierSerializer())
        addDeserializer(RelatedProcessIdentifier::class.java, ProcessIdentifierDeserializer())
    }
}
