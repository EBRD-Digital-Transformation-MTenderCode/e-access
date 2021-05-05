package com.procurement.access.application.model.params

import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.document.DocumentId
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.PartyRole
import com.procurement.access.domain.model.enums.PersonTitle
import com.procurement.access.domain.model.persone.PersonId
import java.time.LocalDateTime

data class PersonesProcessingParams(
    val cpid: Cpid,
    val ocid: Ocid.SingleStage,
    val role: PartyRole,
    val parties: List<Party>
) {
    data class Party(
        val id: String,
        val persones: List<Persone>
    ) {
        data class Persone(
            val id: PersonId,
            val title: PersonTitle,
            val name: String,
            val identifier: Identifier,

            val businessFunctions: List<BusinessFunction>
        ) {
            data class Identifier(
                val scheme: String,
                val id: String,
                val uri: String?
            )

            data class BusinessFunction(
                val id: String,
                val type: BusinessFunctionType,
                val jobTitle: String,
                val period: Period,
                val documents: List<Document>?
            ) {
                data class Period(
                    val startDate: LocalDateTime
                )

                data class Document(
                    val id: DocumentId,
                    val documentType: BusinessFunctionDocumentType,
                    val title: String,
                    val description: String?
                )
            }
        }
    }
}