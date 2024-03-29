package com.procurement.access.application.service.fe.check

import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.CriteriaRelatesTo
import com.procurement.access.domain.model.enums.DocumentType
import com.procurement.access.domain.model.enums.ProcurementMethodModalities
import com.procurement.access.domain.model.enums.QualificationSystemMethod
import com.procurement.access.domain.model.enums.ReductionCriteria
import com.procurement.access.domain.model.requirement.Requirement
import java.time.LocalDateTime

data class CheckFEDataData(
    val tender: Tender,
    val criteria: List<Criterion>
) {
    data class Tender(
        val title: String,
        val description: String,
        val secondStage: SecondStage?,
        val procurementMethodModalities: List<ProcurementMethodModalities>,
        val procurementMethodRationale: String?,
        val procuringEntity: ProcuringEntity?,
        val criteria: List<Criteria>,
        val otherCriteria: OtherCriteria?,
        val documents: List<Document>

    ) {

        data class Criteria(
            val id: String,
            val title: String,
            val relatesTo: CriteriaRelatesTo,
            val description: String?,
            val requirementGroups: List<RequirementGroup>,
            val classification: Classification
        ) {
            data class RequirementGroup(
                val id: String,
                val description: String?,
                val requirements: List<Requirement>
            )

            data class Classification(
                 val id: String,
                 val scheme: String
            )
        }

        data class SecondStage(
            val minimumCandidates: Int?,
            val maximumCandidates: Int?
        )

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

        data class OtherCriteria(
            val reductionCriteria: ReductionCriteria,
            val qualificationSystemMethods: List<QualificationSystemMethod>
        )

        data class Document(
            val id: String,
            val documentType: DocumentType,
            val title: String,
            val description: String?
        )
    }

    data class Criterion(
         val id: String,
         val classification: Classification
    ) {
        data class Classification(
             val id: String,
             val scheme: String
        )
    }
}
