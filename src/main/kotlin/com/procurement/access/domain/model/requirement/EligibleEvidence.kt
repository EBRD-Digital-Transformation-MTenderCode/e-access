package com.procurement.access.domain.model.requirement

import com.procurement.access.infrastructure.handler.v1.model.request.document.RelatedDocumentRequest

data class EligibleEvidence(
    val id: EligibleEvidenceId,
    val title: String,
    val description: String?,
    val type: EligibleEvidenceType,
    val relatedDocument: RelatedDocumentRequest?
)
