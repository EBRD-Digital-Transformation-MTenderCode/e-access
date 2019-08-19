package com.procurement.access.application.service.tender.strategy.prepare.cancellation

import com.procurement.access.model.dto.ocds.DocumentType

data class PrepareCancellationData(
    val amendment: Amendment
) {

    data class Amendment(
        val rationale: String,
        val description: String?,
        val documents: List<Document>?
    ) {

        data class Document(
            val documentType: DocumentType,
            val id: String,
            val title: String,
            val description: String?
        )
    }
}
