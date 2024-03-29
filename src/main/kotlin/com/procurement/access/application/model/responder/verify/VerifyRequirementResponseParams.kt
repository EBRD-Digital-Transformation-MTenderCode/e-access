package com.procurement.access.application.model.responder.verify

import com.procurement.access.application.model.parseCpid
import com.procurement.access.application.model.parseOcid
import com.procurement.access.application.model.parsePersonTitle
import com.procurement.access.application.model.parseStartDate
import com.procurement.access.domain.EnumElementProvider.Companion.keysAsStrings
import com.procurement.access.domain.fail.error.DataErrors
import com.procurement.access.domain.model.Cpid
import com.procurement.access.domain.model.Ocid
import com.procurement.access.domain.model.document.DocumentId
import com.procurement.access.domain.model.enums.BusinessFunctionDocumentType
import com.procurement.access.domain.model.enums.BusinessFunctionType
import com.procurement.access.domain.model.enums.PersonTitle
import com.procurement.access.domain.model.requirement.RequirementId
import com.procurement.access.domain.model.requirement.response.RequirementResponseId
import com.procurement.access.domain.model.requirement.response.RequirementRsValue
import com.procurement.access.domain.model.requirement.tryToRequirementId
import com.procurement.access.lib.functional.Result
import com.procurement.access.lib.functional.Result.Companion.failure
import java.time.LocalDateTime

class VerifyRequirementResponse {

    class Params private constructor(
        val cpid: Cpid,
        val ocid: Ocid.SingleStage,
        val requirementResponseId: RequirementResponseId,
        val value: RequirementRsValue,
        val responder: Responder,
        val requirementId: RequirementId
    ) {
        companion object {
            fun tryCreate(
                cpid: String,
                ocid: String,
                requirementResponseId: String,
                value: RequirementRsValue,
                responder: Responder,
                requirementId: String
            ): Result<Params, DataErrors> {

                val parsedCpid = parseCpid(value = cpid)
                    .onFailure { error -> return error }

                val parsedOcid = parseOcid(value = ocid)
                    .onFailure { error -> return error }

                val parsedRequirementId = requirementId.tryToRequirementId()
                    .onFailure { error -> return error }

                return Result.success(
                    Params(
                        cpid = parsedCpid,
                        ocid = parsedOcid,
                        requirementId = parsedRequirementId,
                        value = value,
                        requirementResponseId = requirementResponseId,
                        responder = responder
                    )
                )
            }
        }

        class Responder private constructor(
            val title: PersonTitle,
            val name: String,
            val identifier: Identifier,
            val businessFunctions: List<BusinessFunction>
        ) {

            companion object {

                private val allowedPersonTitles = PersonTitle.allowedElements.toSet()

                fun tryCreate(
                    title: String,
                    name: String,
                    identifier: Identifier,
                    businessFunctions: List<BusinessFunction>
                ): Result<Responder, DataErrors> {
                    val personTitle = parsePersonTitle(title, allowedPersonTitles, "responder.title")
                        .onFailure { return it }
                    return Result.success(
                        Responder(
                            title = personTitle,
                            name = name,
                            identifier = identifier,
                            businessFunctions = businessFunctions
                        )
                    )
                }
            }

            class Identifier private constructor(
                val scheme: String,
                val id: String,
                val uri: String?
            ) {

                companion object {
                    fun tryCreate(
                        scheme: String,
                        id: String,
                        uri: String?
                    ): Result<Identifier, DataErrors> {

                        return Result.success(
                            Identifier(scheme = scheme, id = id, uri = uri)
                        )
                    }
                }
            }

            class BusinessFunction private constructor(
                val id: String,
                val type: BusinessFunctionType,
                val jobTitle: String,
                val period: Period,
                val documents: List<Document>
            ) {

                companion object {
                    private val allowedBusinessFunctionTypes = BusinessFunctionType.allowedElements
                        .filter {
                            when (it) {
                                BusinessFunctionType.CHAIRMAN,
                                BusinessFunctionType.PROCURMENT_OFFICER,
                                BusinessFunctionType.CONTACT_POINT,
                                BusinessFunctionType.TECHNICAL_EVALUATOR,
                                BusinessFunctionType.TECHNICAL_OPENER,
                                BusinessFunctionType.PRICE_OPENER,
                                BusinessFunctionType.PRICE_EVALUATOR -> true
                                BusinessFunctionType.AUTHORITY -> false
                            }
                        }.toSet()

                    fun tryCreate(
                        id: String,
                        type: String,
                        jobTitle: String,
                        period: Period,
                        documents: List<Document>?
                    ): Result<BusinessFunction, DataErrors> {

                        val parsedType = type
                            .let { type ->
                                BusinessFunctionType.orNull(type)
                                    ?.takeIf { it in allowedBusinessFunctionTypes }
                                    ?: return failure(
                                        DataErrors.Validation.UnknownValue(
                                            name = "businessFunction.type",
                                            expectedValues = allowedBusinessFunctionTypes.keysAsStrings(),
                                            actualValue = type
                                        )
                                    )
                            }

                        return Result.success(
                            BusinessFunction(
                                id = id,
                                type = parsedType,
                                jobTitle = jobTitle,
                                period = period,
                                documents = documents ?: emptyList()
                            )
                        )
                    }
                }

                class Period private constructor(
                    val startDate: LocalDateTime
                ) {

                    companion object {
                        fun tryCreate(
                            startDate: String
                        ): Result<Period, DataErrors> {

                            val startDateParsed = parseStartDate(startDate)
                                .onFailure { return it }

                            return Result.success(
                                Period(startDate = startDateParsed)
                            )
                        }
                    }
                }

                class Document private constructor(
                    val id: DocumentId,
                    val documentType: BusinessFunctionDocumentType,
                    val title: String,
                    val description: String?
                ) {

                    companion object {
                        private val allowedBusinessFunctionDocumentTypes = BusinessFunctionDocumentType.allowedElements
                            .filter {
                                when (it) {
                                    BusinessFunctionDocumentType.REGULATORY_DOCUMENT -> true
                                }
                            }.toSet()

                        fun tryCreate(
                            id: String,
                            documentType: String,
                            title: String,
                            description: String?
                        ): Result<Document, DataErrors> {

                            val createdDocumentType = documentType
                                .let {
                                    BusinessFunctionDocumentType.orNull(it)
                                        ?.takeIf { it in allowedBusinessFunctionDocumentTypes }
                                        ?: return failure(
                                            DataErrors.Validation.UnknownValue(
                                                name = "documentType",
                                                expectedValues = allowedBusinessFunctionDocumentTypes.keysAsStrings(),
                                                actualValue = it
                                            )
                                        )
                                }

                            return Result.success(
                                Document(
                                    id = id,
                                    documentType = createdDocumentType,
                                    title = title,
                                    description = description
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

