package com.procurement.access.application.service.fe.update

import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.DocumentType
import java.time.LocalDateTime

data class AmendFEData(
    val tender: Tender
) {
    data class Tender(
        val title: String,
        val description: String,
        val procurementMethodRationale: String?,
        val procuringEntity: ProcuringEntity?,
        val documents: List<Document>
    ) {
        data class ProcuringEntity(
            val id: String,
            val persons: List<Person>
        ) {
            data class Person(
                val title: String,
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
                    val documents: List<Document>
                ) {
                    data class Document(
                        val id: String,
                        val documentType: BusinessFunctionDocumentType,
                        val title: String,
                        val description: String?
                    )

                    data class Period(
                        val startDate: LocalDateTime
                    )
                }
            }
        }

        data class Document(
            val id: String,
            val documentType: DocumentType,
            val title: String,
            val description: String?
        )
    }
}